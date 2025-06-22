package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.local.dao.UnitDao
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class UnitRepositoryImpl(
    private val unitDao: UnitDao
) : UnitRepository {

    override fun getUnits(query: String): Flow<List<UnitEntity>> {
        return unitDao.getAll(query).map { units ->
            units.filter { !it.isDeletedLocally }
        }
    }

    override suspend fun saveUnit(unit: UnitEntity): Result<Unit> {
        return runCatching {
            unitDao.insertOrUpdate(unit)
        }
    }

    override suspend fun deleteUnit(unit: UnitEntity): Result<Unit> {
        return try {
            val unitToDelete = unit.copy(
                isDeletedLocally = true, isSynced = false, updatedAt = System.currentTimeMillis()
            )
            unitDao.insertOrUpdate(unitToDelete)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}