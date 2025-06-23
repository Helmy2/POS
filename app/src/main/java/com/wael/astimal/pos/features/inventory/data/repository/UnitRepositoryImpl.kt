package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.UnitDao
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.inventory.domain.entity.toEntity
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


class UnitRepositoryImpl(
    private val unitDao: UnitDao
) : UnitRepository {

    override fun getUnits(query: String): Flow<Result<List<ProductUnit>>> {
        return unitDao.getAll(query).map { units ->
            runCatching { units.filter { !it.isDeletedLocally }.map { it.toDomain() } }
        }.catch { Result.failure<List<ProductUnit>>(it) }
    }

    override suspend fun saveUnit(unit: ProductUnit): Result<Unit> {
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
                isDeletedLocally = true, isSynced = false, updatedAt = System.currentTimeMillis()
            )
            unitDao.insertOrUpdate(unitToDelete)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}