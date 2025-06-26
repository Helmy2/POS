package com.wael.astimal.pos.core.data

import com.wael.astimal.pos.core.data.remote.SyncApiService
import com.wael.astimal.pos.core.data.remote.dto.SyncRequest
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.remote.dto.toEntity
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import com.wael.astimal.pos.features.management.data.remote.dto.toEntity
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
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
    private val partnerRepository: BusinessPartnerRepository,
) : SyncService {

    override suspend fun performFullSync(): Result<Unit> {
        return try {
            val lastSyncDate = syncManager.getLastSyncDate().first()
            val syncRequest = SyncRequest(lastSyncDate = lastSyncDate)

            syncUnits(syncRequest)
            syncEmployee(syncRequest)
            syncPartners(syncRequest)

//            syncManager.updateLastSyncDate(response.data.nextSyncDate)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun syncUnits(syncRequest: SyncRequest) {
        val unitResult = syncApiService.syncUnits(syncRequest)
        unitRepository.syncWithServer(
            unitResult.getOrThrow().data.units.map { it.toEntity() },
        )
    }

    private suspend fun syncEmployee(syncRequest: SyncRequest) {
        val employeeResult = syncApiService.syncEmployees(syncRequest)
        val result = employeeResult.getOrThrow().data.employees.map { it.toEntities() }
        val (userEntities, storeEntities) = result.unzip()

        storeRepository.syncWithServer(storeEntities.filterNotNull())
        userRepository.syncWithServer(userEntities)

        assignStoreToEmployee(result)
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

    private suspend fun syncPartners(syncRequest: SyncRequest) {
        val clientsResult = syncApiService.syncClients(syncRequest)
        val supplierResult = syncApiService.syncSuppliers(syncRequest)

        val data = clientsResult.getOrThrow().data.clients.map {
            it.toEntity()
        } + supplierResult.getOrThrow().data.suppliers.map { it.toEntity() }

        partnerRepository.syncWithServer(data)
    }
}

interface SyncService {
    suspend fun performFullSync(): Result<Unit>
}
