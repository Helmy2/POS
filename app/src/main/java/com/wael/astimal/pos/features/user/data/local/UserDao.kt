package com.wael.astimal.pos.features.user.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :localId")
    suspend fun getUserById(localId: Int): UserEntity?

    @Query("SELECT * FROM users WHERE isEmployeeFlag = 1")
    fun getAllEmployeesFlow(): Flow<List<UserEntity>>

    @Query(
        """
        SELECT u.* FROM users u
        LEFT JOIN clients c ON u.id = c.userId
        WHERE u.isClientFlag = 1 AND c.localId IS NULL 
        AND (u.arName LIKE '%' || :query || '%' OR u.enName LIKE '%' || :query || '%')
    """
    )
    fun getPotentialUsersToBecomeClientsFlow(query: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isSynced = 0")
    suspend fun getUnsyncedUsers(): List<UserEntity>

    @Query("DELETE FROM users WHERE id IN (:localIds)")
    suspend fun deleteUsersByLocalIds(localIds: List<Long>)
}