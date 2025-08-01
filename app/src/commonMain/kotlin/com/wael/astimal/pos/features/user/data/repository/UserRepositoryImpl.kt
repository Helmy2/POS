package com.wael.astimal.pos.features.user.data.repository

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.toDateString
import com.wael.astimal.pos.features.user.data.local.SettingsManager
import com.wael.astimal.pos.features.user.data.local.UserDao
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.entity.toDomain
import com.wael.astimal.pos.features.user.data.remote.ProfileApiService
import com.wael.astimal.pos.features.user.data.remote.dto.ProfileDto
import com.wael.astimal.pos.features.user.data.remote.dto.toEntity
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val supabaseClient: SupabaseClient,
    private val settingsManager: SettingsManager,
    private val profileApiService: ProfileApiService,
    private val adminClient: suspend () -> SupabaseClient,
) : UserRepository {

    override fun getEmployeesFlow(): Flow<List<User>> {
        return userDao.getAllEmployeesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCurrentUser(): User? {
        val id = settingsManager.getUserId() ?: return null
        val user = userDao.getUserBySupabaseId(id).first()
        return user?.toDomain()
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return settingsManager.getUserId() != null
    }


    override suspend fun login(email: String, password: String): Result<User> {
        return try {
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
                val syncedUserEntity = syncProfile(profileDto)
                return Result.success(syncedUserEntity.toDomain())
            }.onFailure {
                return Result.failure(it)
            }

            Result.failure(Exception("Unknown error during profile fetch."))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }


    private suspend fun syncProfile(profileDto: ProfileDto): UserEntity {
        val existingUser = userDao.getUserBySupabaseId(profileDto.id).first()
        val userEntity = profileDto.toEntity().copy(
            id = existingUser?.id!!,
            createdAt = existingUser.createdAt
        )
        userDao.upsert(userEntity)
        return userEntity
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            settingsManager.changeUserId("")
            supabaseClient.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUser(
        email: String,
        arName: String,
        enName: String,
        password: String,
    ): Result<Unit> {
        return try {
            val currentUser = getCurrentUser() ?: return Result.failure(Exception("Not logged in."))

            if (!currentUser.isAdmin) {
                return Result.failure(Exception("You are not an admin."))
            }

            if (password.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters long."))
            }

            val userWithEmail = adminClient().auth.admin.createUserWithEmail {
                this.email = email
                this.password = password
                autoConfirm = true
            }

            val profileDto = ProfileDto(
                id = userWithEmail.id,
                username = enName,
                arName = arName,
                enName = enName,
                isAdmin = false,
                updatedAt = Clock.now().toDateString(),
                avatarUrl = "https://ofzbmodzxgbpvybfhofr.supabase.co/storage/v1/object/public/bucket//avatar_profile.png",
                fcmToken = null,
                email = email,
                // TODO: canHandlePrivatePartner
                canHandlePrivatePartner = false
            )

            supabaseClient.postgrest["profiles"].upsert(profileDto)

            val userEntity = profileDto.toEntity()
            userDao.upsert(userEntity)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(
        id: String,
        arName: String,
        enName: String,
        password: String?,
        email: String?
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

            // Update profile data in Postgrest
            val updates = mapOf(
                "ar_name" to arName,
                "en_name" to enName,
                "username" to enName,
                "email" to email,
                "updated_at" to Clock.now().toDateString()
            )
            supabaseClient.postgrest["profiles"].update(updates) { filter { eq("id", id) } }

            // Update local database
            val localUser = userDao.getUserBySupabaseId(id).first()
            if (localUser != null) {
                userDao.upsert(localUser.copy(arName = arName, enName = enName, username = enName))
            }

            Result.success(Unit)
        } catch (e: Exception) {
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
            supabaseClient.postgrest["profiles"].delete { filter { eq("id", id) } }

            // Delete from local database
            val localUser = userDao.getUserBySupabaseId(id).first()
            if (localUser != null) {
                userDao.delete(localUser.id)
            }

            Result.success(Unit)
        } catch (e: Exception) {
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
}
