package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import kotlinx.coroutines.flow.Flow

interface UnitRepository {
    fun getUnits(query: String): Flow<Result<List<ProductUnit>>>
    suspend fun saveUnit(unit: ProductUnit): Result<Unit>
    suspend fun deleteUnit(unit: ProductUnit): Result<Unit>
}