package com.wael.astimal.pos.core.data

import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.data.remote.SyncApiService
import com.wael.astimal.pos.core.data.remote.dto.SyncRequest
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.remote.dto.toEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.inventory.domain.repository.CategoryRepository
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import com.wael.astimal.pos.features.management.data.entity.OrderEntity
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.data.remote.dto.toEntity
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.SalesOrderRepository
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.domain.repository.UserRepository


class SyncServiceImpl(
    private val syncApiService: SyncApiService,
    private val syncManager: SyncManager,
    private val unitRepository: UnitRepository,
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository,
    private val partnerRepository: BusinessPartnerRepository,
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val salesOrderRepository: SalesOrderRepository,
    private val navigationController: NavigationController
) : SyncService {

    override suspend fun performFullSync(): Result<Unit> {
        return try {
            // TODO Remove
            userRepository.login(
                "admin@mail.com",
                "adminadmin"
            ).getOrThrow()
            navigationController.navigate(
                Destination.Dashboard,
                popUpToRoute = Destination.Login,
            )

            userRepository.getCurrentUser() ?: throw Exception("User not authenticated")
//
//            val lastSyncDate = syncManager.getLastSyncDate().first()
//            val syncRequest = SyncRequest(lastSyncDate = lastSyncDate)
//
//            syncUnits(syncRequest)
//            syncEmployee(syncRequest)
//            syncPartners(syncRequest)
//            syncCategory(syncRequest)
//            syncProducts(syncRequest)
//            syncOrders(syncRequest)

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
        ).getOrThrow()
    }

    private suspend fun syncEmployee(syncRequest: SyncRequest) {
//        val employeeResult = syncApiService.syncEmployees(syncRequest)
//        val result = employeeResult.getOrThrow().data.employees.map { it.toEntities() }
//        val (userEntities, storeEntities) = result.unzip()
//
//        storeRepository.syncWithServer(storeEntities.filterNotNull())
//        userRepository.syncWithServer(userEntities)
//
//        assignStoreToEmployee(result)
    }

    private suspend fun assignStoreToEmployee(result: List<Pair<UserEntity, StoreEntity?>>) {
        result.forEach { (userEntity, storeEntity) ->
            val userId = userEntity.id
            val storeId =
                fetchOrSaveStore(storeEntity?.serverId ?: throw Exception("Store not found"))
            userRepository.assignStoreToEmployee(userId, storeId).getOrThrow()
        }
    }

    private suspend fun syncPartners(syncRequest: SyncRequest) {
        val clientsResult = syncApiService.syncClients(syncRequest)
        val supplierResult = syncApiService.syncSuppliers(syncRequest)

        val data = clientsResult.getOrThrow().data.clients.map {
            it.toEntity()
        } + supplierResult.getOrThrow().data.suppliers.map { it.toEntity() }

        partnerRepository.syncWithServer(data).getOrThrow()
    }

    private suspend fun syncCategory(syncRequest: SyncRequest) {
        val categoryResult = syncApiService.syncCategories(syncRequest)
        val data = categoryResult.getOrThrow().data.categories
        categoryRepository.syncWithServer(data).getOrThrow()
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

    private suspend fun syncOrders(syncRequest: SyncRequest) {
        //TODO push orders
//        salesOrderRepository.getLocalChanges()

        // saving orders
        val orderResult = syncApiService.syncOrders(syncRequest).getOrThrow().data.orders
        val data = orderResult.map { orderDto ->
            OrderEntity(
                serverId = orderDto.id,
                orderDate = 0L,
                isSynced = true,
                isDeletedLocally = false,
                createdAt = orderDto.createdAt.parseIsoTimestamp() ?: Clock.now(),
                updatedAt = orderDto.updatedAt.parseIsoTimestamp() ?: Clock.now(),
                invoiceNumber = orderDto.invoiceNumber,
                amountPaid = orderDto.totalPrice - orderDto.remaining,
                amountRemaining = orderDto.remaining,
                totalAmount = orderDto.totalPrice,
                paymentType = PaymentType.getFormServerValue(orderDto.paymentType),
                businessPartnerLocalId = fetchBusinessPartner(orderDto.sourceId),
                employeeLocalId = orderDto.employeeId,
            ) to orderDto.orderProducts.map { orderItemDto ->
                val product = fetchProduct(orderItemDto.productId)
                var quantity = orderItemDto.quantity
                var price = orderItemDto.price
                if (product.minimumProductUnit?.id?.server == orderItemDto.unitId) {
                    quantity = product.convertToMaxUnitQuantity(quantity)
                    price = product.convertToMaxUnitPrice(price)
                }
                OrderProductEntity(
                    serverId = orderItemDto.id,
                    productLocalId = product.id.local,
                    quantity = quantity,
                    unitSellingPrice = price,
                    orderLocalId = 0L,
                    itemTotalPrice = orderItemDto.totalPrice,
                )
            }
        }
        salesOrderRepository.syncWithServer(data).getOrThrow()
    }

    private suspend fun fetchProduct(serverId: Long): Product {
        return productRepository.getProductByServerId(serverId).getOrThrow()
    }

    private suspend fun fetchBusinessPartner(serverId: Long): Long {
        return partnerRepository.getBusinessPartnerByServerId(serverId).getOrThrow().localId
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
