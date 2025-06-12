package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.domain.entity.EmployeeAccount
import kotlinx.coroutines.flow.Flow

interface EmployeeAccountRepository {
    fun getEmployeeAccount(employeeId: Long): Flow<EmployeeAccount?>
}