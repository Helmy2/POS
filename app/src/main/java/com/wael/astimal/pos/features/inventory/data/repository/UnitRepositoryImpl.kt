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

    override suspend fun getUnitByLocalId(localId: String): UnitEntity? {
        val unit = unitDao.getByLocalId(localId)
        return if (unit?.isDeletedLocally == true) null else unit
    }

    override suspend fun getUnitByServerId(serverId: Int): UnitEntity? {
        val unit = unitDao.getByServerId(serverId)
        return if (unit?.isDeletedLocally == true) null else unit
    }

    override suspend fun addUnit(unit: UnitEntity): Result<UnitEntity> {
        return try {
            val unitToInsert = unit.copy(
                serverId = null,
                isSynced = false,
                lastModified = System.currentTimeMillis(),
                isDeletedLocally = false
            )
            unitDao.insert(listOf(unitToInsert))
            Result.success(unitToInsert)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUnit(unit: UnitEntity): Result<UnitEntity> {
        return try {
            val unitToUpdate = unit.copy(
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )
            unitDao.insert(listOf(unitToUpdate))
            Result.success(unitToUpdate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUnit(unit: UnitEntity): Result<Unit> {
        return try {
            val unitToDelete = unit.copy(
                isDeletedLocally = true,
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )
            unitDao.insert(listOf(unitToDelete))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncUnits(): Result<Unit> {
        // todo
        // Placeholder: This method will be implemented when the API is ready.
        // For now, it does nothing or could log that sync is not yet available.
        println("UnitRepositoryImpl: syncUnits() called, but API service is not yet integrated.")
        // Simulate success for now, or return a specific result indicating sync is pending/unavailable.
        return Result.success(Unit)
    }
}