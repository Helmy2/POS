package com.wael.astimal.pos.features.reports.domain.repository

import com.wael.astimal.pos.features.reports.domain.model.CurrentStockInfo
import kotlinx.coroutines.flow.Flow

interface CurrentStockRepository {
    fun getCurrentStock(
        productId: String?,
        storeId: String?
    ): Flow<List<CurrentStockInfo>>
}