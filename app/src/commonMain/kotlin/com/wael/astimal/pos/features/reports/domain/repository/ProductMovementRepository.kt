package com.wael.astimal.pos.features.reports.domain.repository

import com.wael.astimal.pos.features.reports.domain.model.ProductMovementGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ProductMovementRepository {
    fun getProductMovement(
        startDate: LocalDate,
        endDate: LocalDate,
        productId: String?,
        storeId: String?,
    ): Flow<List<ProductMovementGroup>>
}