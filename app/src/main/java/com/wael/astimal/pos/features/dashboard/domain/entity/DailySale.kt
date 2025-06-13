package com.wael.astimal.pos.features.dashboard.domain.entity

import java.time.LocalDate

data class DailySale(
    val date: LocalDate?,
    val totalRevenue: Double,
    val numberOfSales: Int
)
