package com.wael.astimal.pos.core.data

import com.wael.astimal.pos.core.data.remote.SyncApiService
import com.wael.astimal.pos.core.data.remote.dto.SyncRequest
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.remote.dto.toEntities
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.first


class SyncServiceImpl(
    private val syncApiService: SyncApiService,
    private val syncManager: SyncManager,
    private val unitRepository: UnitRepository,
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository,
) : SyncService {

    override suspend fun performFullSync(): Result<Unit> {
        return try {
            val lastSyncDate = syncManager.getLastSyncDate().first()
            val syncRequest = SyncRequest(lastSyncDate = lastSyncDate)

            syncUnits(syncRequest)
            syncEmployee(syncRequest)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun syncUnits(syncRequest: SyncRequest): Result<Unit> {
        val unitResult = syncApiService.syncUnits(syncRequest)

        unitResult.onSuccess { response ->
            unitRepository.syncWithServer(response.data.units)

            // IMPORTANT: Update the sync date only after a successful sync
            syncManager.updateLastSyncDate(response.data.nextSyncDate)
        }.onFailure {
            // If one sync fails, we stop the entire process
            return Result.failure(it)
        }
        return Result.success(Unit)
    }

    private suspend fun syncEmployee(syncRequest: SyncRequest): Result<Unit> {
        val employeeResult = syncApiService.syncEmployees(syncRequest)
        employeeResult.onSuccess { response ->
            val result = response.data.employees.map { it.toEntities() }
            val (userEntities, storeEntities) = result.unzip()

            storeRepository.syncWithServer(storeEntities.filterNotNull())
            userRepository.syncWithServer(userEntities)

            assignStoreToEmployee(result)

            syncManager.updateLastSyncDate(response.data.nextSyncDate)
        }.onFailure { return Result.failure(it) }

        return Result.success(Unit)
    }

    private suspend fun assignStoreToEmployee(result: List<Pair<UserEntity, StoreEntity?>>) {
        result.forEach { (userEntity, storeEntity) ->
            val userId = userEntity.id
            val storeId = storeRepository.getStoreBySeverId(
                storeEntity?.serverId ?: throw Exception("Store not found")
            ).getOrThrow().id.local
            userRepository.assignStoreToEmployee(
                userId,
                storeId
            )
        }
    }
}

interface SyncService {
    suspend fun performFullSync(): Result<Unit>
}
