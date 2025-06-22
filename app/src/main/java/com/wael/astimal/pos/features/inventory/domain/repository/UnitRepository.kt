package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

interface UnitRepository {
    fun getUnits(query: String): Flow<List<UnitEntity>>
    suspend fun saveUnit(unit: UnitEntity): Result<Unit>
    suspend fun deleteUnit(unit: UnitEntity): Result<Unit>
}