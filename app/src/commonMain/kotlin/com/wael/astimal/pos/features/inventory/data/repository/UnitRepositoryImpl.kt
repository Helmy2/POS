package com.wael.astimal.pos.features.inventory.data.repository

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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


class UnitRepositoryImpl(
    private val unitDao: UnitDao,
    private val supabaseClient: SupabaseClient
) : UnitRepository {

    override fun getUnits(query: String): Flow<List<ProductUnit>> {
        return unitDao.getAll(query).map { units ->
            units.filter { !it.isDeletedLocally }.map { it.toDomain() }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveUnit(unit: ProductUnit): Result<Long> {
        return try {
            val entity = unit.toDto()

            val result = if (unit.id == "") {
                supabaseClient.from("units").insert(
                    entity.copy(id = Uuid.random().toString())
                ) {
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

            val localId = unitDao.insertOrUpdate(result.toEntity())

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
                    eq("id", unit.id)
                }
            }
            unitDao.deleteUnitByLocalId(unit.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncWithServer(units: List<UnitEntity>): Result<Unit> {
        return runCatching {
            unitDao.deleteAll()
            unitDao.upsertAll(units)
        }
    }

    override suspend fun getUnitByServerId(
        id: String
    ): Result<ProductUnit> {
        return runCatching {
            unitDao.getUnitByServerId(id)?.toDomain() ?: throw Exception("Unit not found")
        }
    }
}