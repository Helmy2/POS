package com.wael.astimal.pos.features.user.data.repository

import com.wael.astimal.pos.core.data.SyncManager
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.deleteRecordAndLog
import com.wael.astimal.pos.core.util.toISOString
import com.wael.astimal.pos.features.user.data.local.SettingsManager
import com.wael.astimal.pos.features.user.data.local.UserDao
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.entity.toDomain
import com.wael.astimal.pos.features.user.data.remote.ProfileApiService
import com.wael.astimal.pos.features.user.data.remote.dto.ProfileDto
import com.wael.astimal.pos.features.user.data.remote.dto.toEntity
import com.wael.astimal.pos.features.user.domain.PermissionManager
import com.wael.astimal.pos.features.user.domain.entity.PermissionDetails
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val supabaseClient: SupabaseClient,
    private val settingsManager: SettingsManager,
    private val profileApiService: ProfileApiService,
    private val syncManager: SyncManager,
    private val adminClient: suspend () -> SupabaseClient,
) : UserRepository {

    override fun getEmployeesFlow(): Flow<List<User>> {
        return userDao.getAllEmployeesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCurrentUser(): User? {
        val id = settingsManager.getUserId() ?: return null
        val user = userDao.getUserBySupabaseId(id).first()?.toDomain()
        PermissionManager.updatePermissions(user)
        return user
    }


    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            syncManager.restSyncDate()

            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val supabaseUser = supabaseClient.auth.currentUserOrNull()
                ?: return Result.failure(Exception("Could not retrieve user after login."))

            supabaseClient.postgrest["profiles"]
                .update(
                    buildJsonObject { put("fcm_token", settingsManager.getFcmToken()) }
                ) {
                    filter {
                        eq("id", supabaseUser.id)
                    }
                }

            settingsManager.changeUserId(supabaseUser.id)

            val profileResult = profileApiService.getProfile(supabaseUser.id)

            profileResult.onSuccess { profileDto ->
                userDao.upsert(profileDto.toEntity())
            }.onFailure {
                return Result.failure(it)
            }

            PermissionManager.updatePermissions(getCurrentUser())

            syncManager.requestSync()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            syncManager.restSyncDate()
            settingsManager.changeUserId("")
            supabaseClient.auth.signOut()
            PermissionManager.updatePermissions(null)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun createUser(
        email: String,
        arName: String,
        enName: String,
        password: String,
        canHandlePrivatePartner: Boolean,
        permissions: Map<String, PermissionDetails>
    ): Result<Unit> {

        return try {
            val currentUser = getCurrentUser() ?: return Result.failure(Exception("Not logged in."))

            if (!currentUser.isAdmin) {
                return Result.failure(Exception("You are not an admin."))
            }

            if (password.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters long."))
            }

            val usersWithSameArabicName =
                userDao.getAllEmployeesFlow().first().any { it.arName == arName }
            if (usersWithSameArabicName) {
                return Result.failure(Exception("User with the same Arabic name already exists."))
            }

            val userWithEmail = adminClient().auth.admin.createUserWithEmail {
                this.email = email
                this.password = password
                autoConfirm = true
            }

            val permissionsJson = permissions.let {
                try {
                    Json.encodeToString(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            val profileDto = ProfileDto(
                id = userWithEmail.id,
                username = enName,
                arName = arName,
                enName = enName,
                isAdmin = false,
                updatedAt = Clock.now().toISOString(),
                avatarUrl = "https://ofzbmodzxgbpvybfhofr.supabase.co/storage/v1/object/public/bucket//avatar_profile.png",
                fcmToken = null,
                email = email,
                canHandlePrivatePartner = canHandlePrivatePartner,
                permissions = permissionsJson
            )

            supabaseClient.postgrest["profiles"].upsert(profileDto)

            val userEntity = profileDto.toEntity()
            userDao.upsert(userEntity)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateUser(
        id: String,
        arName: String,
        enName: String,
        password: String?,
        email: String?,
        canHandlePrivatePartner: Boolean,
        permissions: Map<String, PermissionDetails>
    ): Result<Unit> {
        return try {
            val currentUser = getCurrentUser() ?: return Result.failure(Exception("Not logged in."))
            if (!currentUser.isAdmin) {
                return Result.failure(Exception("You are not an admin."))
            }

            // Update password if provided
            if (password != null) {
                if (password.length < 6) {
                    return Result.failure(Exception("Password must be at least 6 characters long."))
                }
                adminClient().auth.admin.updateUserById(id) {
                    this.password = password
                }
            }

            // Update email if provided
            if (email != null) {
                adminClient().auth.admin.updateUserById(id) {
                    this.email = email
                }
            }

            val permissionsJson = permissions.let {
                try {
                    Json.encodeToString(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            // Update profile data in Postgrest
            val profileUpdates = JsonObject(
                mapOf(
                    "ar_name" to JsonPrimitive(arName),
                    "en_name" to JsonPrimitive(enName),
                    "username" to JsonPrimitive(enName),
                    "email" to JsonPrimitive(email),
                    "can_handle_private_partner" to JsonPrimitive(canHandlePrivatePartner),
                    "updated_at" to JsonPrimitive(Clock.now().toISOString()),
                    "permissions" to if (permissionsJson != null) JsonPrimitive(permissionsJson) else JsonPrimitive(
                        ""
                    ),
                )
            )

            supabaseClient.postgrest["profiles"].update(
                profileUpdates
            ) { filter { eq("id", id) } }

            // Update local database
            val localUser = userDao.getUserBySupabaseId(id).first()
            if (localUser != null) {
                userDao.upsert(localUser.copy(arName = arName, enName = enName, username = enName))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(id: String): Result<Unit> {
        return try {
            val currentUser = getCurrentUser() ?: return Result.failure(Exception("Not logged in."))
            if (!currentUser.isAdmin) {
                return Result.failure(Exception("You are not an admin."))
            }

            // Delete from Supabase Auth
            adminClient().auth.admin.deleteUser(id)

            // Delete from Postgrest "profiles" table
            supabaseClient.deleteRecordAndLog(
                targetTableName = "profiles",
                targetRecordId = id
            )

            // Delete from local database
            val localUser = userDao.getUserBySupabaseId(id).first()
            if (localUser != null) {
                userDao.hardDelete(localUser.id)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getUserByServerId(id: String): Result<User?> {
        return runCatching {
            userDao.getUserBySupabaseId(id).first()?.toDomain()
        }
    }

    override suspend fun syncWithServer(users: List<UserEntity>): Result<Unit> {
        return runCatching {
            userDao.upsertAll(users)
        }
    }

    override suspend fun getAdmin(): User? {
        return userDao.getAdmin()?.toDomain()
    }

    override suspend fun deleteAll(ids: List<String>): Result<Unit> {
        return try {
            ids.forEach { userDao.hardDelete(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
