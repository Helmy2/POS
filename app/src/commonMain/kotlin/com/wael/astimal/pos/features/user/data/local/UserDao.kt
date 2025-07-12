package com.wael.astimal.pos.features.user.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.domain.entity.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE role = :role")
    fun getAllEmployeesFlow(
        role: UserRole = UserRole.EMPLOYEE
    ): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<UserEntity>)

    @Query("SELECT * FROM users WHERE supabaseId = :supabaseId LIMIT 1")
    fun getUserBySupabaseId(supabaseId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :localId LIMIT 1")
    suspend fun getUserById(localId: Long): UserEntity?
}