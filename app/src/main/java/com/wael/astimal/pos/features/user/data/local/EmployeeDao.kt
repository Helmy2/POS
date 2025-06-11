package com.wael.astimal.pos.features.user.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.user.data.entity.EmployeeStoreEntity

@Dao
interface EmployeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun assignStoreToEmployee(assignment: EmployeeStoreEntity)

    @Query("""
        SELECT storeLocalId FROM employee_stores
        WHERE employeeLocalId = :employeeId
        LIMIT 1
    """)
    suspend fun getStoreIdForEmployee(employeeId: Long): Long?
}
