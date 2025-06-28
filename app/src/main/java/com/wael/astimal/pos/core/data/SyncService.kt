package com.wael.astimal.pos.core.data

import com.wael.astimal.pos.core.data.remote.SyncApiService
import com.wael.astimal.pos.core.data.remote.dto.SyncRequest
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.remote.dto.toEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.inventory.domain.repository.CategoryRepository
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
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
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository
) : SyncService {

    override suspend fun performFullSync(): Result<Unit> {
        return try {
            val lastSyncDate = syncManager.getLastSyncDate().first()
            val syncRequest = SyncRequest(lastSyncDate = lastSyncDate)

            syncUnits(syncRequest)
            syncEmployee(syncRequest)
            syncPartners(syncRequest)
            syncCategory(syncRequest)
            syncProducts(syncRequest)

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
            val storeId =
                fetchOrSaveStore(storeEntity?.serverId ?: throw Exception("Store not found"))
            userRepository.assignStoreToEmployee(userId, storeId)
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

    private suspend fun syncCategory(syncRequest: SyncRequest) {
        val categoryResult = syncApiService.syncCategories(syncRequest)
        val data = categoryResult.getOrThrow().data.categories
        categoryRepository.syncWithServer(data)
    }

    private suspend fun syncProducts(syncRequest: SyncRequest) {
        val productResult = syncApiService.syncProducts(syncRequest).getOrThrow().data.products
        val data = productResult.map {
            val storeId = fetchOrSaveStore(it.storeId)
            val categoryId = fetchOrSaveCategory(it.categoryId)
            val maximumUnitId = fetchOrSaveUnit(it.maximumUnitId)
            val minimumUnitId = it.minimumUnitId?.let { serverId -> fetchOrSaveUnit(serverId) }

            it.toEntity(
                categoryId = categoryId,
                storeId = storeId,
                maximumUnitId = maximumUnitId,
                minimumUnitId = minimumUnitId,
            )
        }
        productRepository.syncWithServer(data)
    }

    private suspend fun fetchOrSaveStore(serverId: Long): Long {
        return storeRepository.getStoreBySeverId(serverId).getOrNull()?.id?.local
            ?: storeRepository.saveStore(Store.getUnspecifiedStore(serverId = serverId))
                .getOrThrow()
    }

    private suspend fun fetchOrSaveCategory(serverId: Long): Long {
        return categoryRepository.getCategoryByServerId(serverId).getOrNull()?.id?.local
            ?: categoryRepository.saveCategory(Category.getUnspecifiedCategory(serverId = serverId))
                .getOrThrow()
    }

    private suspend fun fetchOrSaveUnit(serverId: Long): Long {
        return unitRepository.getUnitByServerId(serverId).getOrNull()?.id?.local
            ?: unitRepository.saveUnit(ProductUnit.getUnspecifiedUnit(serverId = serverId))
                .getOrThrow()
    }
}

interface SyncService {
    suspend fun performFullSync(): Result<Unit>
}
