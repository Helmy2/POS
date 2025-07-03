package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import kotlinx.coroutines.flow.Flow

interface UnitRepository {
    fun getUnits(query: String): Flow<List<ProductUnit>>
    suspend fun saveUnit(unit: ProductUnit): Result<Long>
    suspend fun deleteUnit(unit: ProductUnit): Result<Unit>
    suspend fun syncWithServer(units: List<UnitEntity>): Result<Unit>
    suspend fun getUnitByServerId(id: Long): Result<ProductUnit>
}