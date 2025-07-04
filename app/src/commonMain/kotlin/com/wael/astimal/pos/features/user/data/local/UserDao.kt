package com.wael.astimal.pos.features.user.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wael.astimal.pos.features.user.data.entity.EmployeeStoreEntity
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.domain.entity.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrUpdate(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :localId")
    suspend fun getUserById(localId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :localId")
    fun getUserFLowById(localId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE role = :role")
    fun getAllEmployeesFlow(
        role: UserRole = UserRole.EMPLOYEE
    ): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isSynced = 0")
    suspend fun getUnsyncedUsers(): List<UserEntity>

    @Query("DELETE FROM users WHERE id IN (:localIds)")
    suspend fun deleteUsersByLocalIds(localIds: List<Long>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun assignStoreToEmployee(assignment: EmployeeStoreEntity)

    @Query(
        """
        SELECT storeLocalId FROM employee_stores
        WHERE employeeLocalId = :employeeId
        LIMIT 1
    """
    )
    suspend fun getStoreIdForEmployee(employeeId: Long?): Long?

    //findByEmail
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun findByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<UserEntity>)

    @Query("SELECT * FROM users WHERE supabaseId = :supabaseId LIMIT 1")
    fun getUserBySupabaseId(supabaseId: String): Flow<UserEntity?>
}