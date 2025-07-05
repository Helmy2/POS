package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.local.dao.UnitDao
import com.wael.astimal.pos.features.inventory.data.local.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.inventory.domain.entity.toEntity
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class UnitRepositoryImpl(
    private val unitDao: UnitDao
) : UnitRepository {

    override fun getUnits(query: String): Flow<List<ProductUnit>> {
        return unitDao.getAll(query).map { units ->
            units.filter { !it.isDeletedLocally }.map { it.toDomain() }
        }
    }

    override suspend fun saveUnit(unit: ProductUnit): Result<Long> {
        return runCatching {
            if (unit.name.arName.isNullOrBlank() && unit.name.arName.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("Arabic and English names must be provided."))
            }
            unitDao.insertOrUpdate(unit.toEntity())
        }
    }

    override suspend fun deleteUnit(unit: ProductUnit): Result<Unit> {
        return try {
            val unitToDelete = unit.toEntity().copy(
                isDeletedLocally = true, isSynced = false, updatedAt = Clock.now()
            )
            unitDao.insertOrUpdate(unitToDelete)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncWithServer(units: List<UnitEntity>): Result<Unit> {
        return runCatching {
            val entities = units.map { entity ->
                val existingEntity = unitDao.getUnitByServerId(
                    entity.serverId ?: throw Exception("Server id is null")
                )
                entity.copy(
                    localId = existingEntity?.localId ?: 0L
                )
            }
            unitDao.upsertAll(entities)
        }
    }

    override suspend fun getUnitByServerId(
        id: Long
    ): Result<ProductUnit> {
        return runCatching {
            unitDao.getUnitByServerId(id)?.toDomain() ?: throw Exception("Unit not found")
        }
    }
}