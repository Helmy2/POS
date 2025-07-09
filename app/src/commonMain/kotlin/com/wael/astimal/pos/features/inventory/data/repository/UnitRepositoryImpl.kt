package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.features.inventory.data.local.dao.UnitDao
import com.wael.astimal.pos.features.inventory.data.local.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.remote.dto.UnitDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.toEntity
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.inventory.domain.entity.toDto
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class UnitRepositoryImpl(
    private val unitDao: UnitDao,
    private val supabaseClient: SupabaseClient
) : UnitRepository {

    override fun getUnits(query: String): Flow<List<ProductUnit>> {
        return unitDao.getAll(query).map { units ->
            units.filter { !it.isDeletedLocally }.map { it.toDomain() }
        }
    }

    override suspend fun saveUnit(unit: ProductUnit): Result<Long> {
        return try {
            val entity = unit.toDto()

            val result = if (unit.id == Id.new) {
                supabaseClient.from("units").insert(entity) {
                    select()
                }.decodeSingle<UnitDto>()
            } else {
                supabaseClient.from("units").update(entity) {
                    filter {
                        eq("id", entity.id)
                    }
                    select()
                }.decodeSingle<UnitDto>()
            }

            val localId = unitDao.insertOrUpdate(
                result.toEntity().copy(localId = unit.id.local)
            )

            Result.success(localId)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteUnit(unit: ProductUnit): Result<Unit> {
        return try {
            supabaseClient.from("units").delete {
                filter {
                    eq("id", unit.id.server!!)
                }
            }
            unitDao.deleteUnitByLocalId(unit.id.local)
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