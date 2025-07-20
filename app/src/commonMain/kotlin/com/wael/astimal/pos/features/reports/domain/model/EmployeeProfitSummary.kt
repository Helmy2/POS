package com.wael.astimal.pos.features.reports.domain.model

import kotlinx.datetime.LocalDate

/**
 * Represents the daily profit commission summary for an employee.
 */
data class EmployeeProfitSummary(
    val date: LocalDate,
    val directCommission: Double,      // Commission from invoices created by the employee
    val responsibilityCommission: Double, // Commission from invoices for customers the employee is responsible for
    val totalCommission: Double
)