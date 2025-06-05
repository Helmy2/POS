package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

interface UnitRepository {
    fun getUnits(query: String): Flow<List<UnitEntity>>
    suspend fun getUnitByLocalId(localId: String): UnitEntity?
    suspend fun getUnitByServerId(serverId: Int): UnitEntity?
    suspend fun addUnit(unit: UnitEntity): Result<UnitEntity>
    suspend fun updateUnit(unit: UnitEntity): Result<UnitEntity>
    suspend fun deleteUnit(unit: UnitEntity): Result<Unit>
}