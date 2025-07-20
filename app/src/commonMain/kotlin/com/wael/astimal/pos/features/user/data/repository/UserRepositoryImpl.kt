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

            settingsManager.changeUserId(supabaseUser.id)

            val profileResult = profileApiService.getProfile(supabaseUser.id)

            profileResult.onSuccess { profileDto ->
                // Step 4: Sync the fetched profile with the local database
                val syncedUserEntity = syncProfile(profileDto)
                return Result.success(syncedUserEntity.toDomain())
            }.onFailure {
                // If fetching the profile fails, the login is considered incomplete.
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
                avatarUrl = "",
                fcmToken = null
            )

            supabaseClient.postgrest["profiles"].upsert(profileDto)

            val userEntity = profileDto.toEntity()
            userDao.upsert(userEntity)

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

    override suspend fun getUserById(id: Long): Result<User?> {
        return runCatching {
            userDao.getUserById(id)?.toDomain()
        }
    }

    override suspend fun syncWithServer(users: List<UserEntity>): Result<Unit> {
        return runCatching {
            userDao.upsertAll(users)
        }
    }
}