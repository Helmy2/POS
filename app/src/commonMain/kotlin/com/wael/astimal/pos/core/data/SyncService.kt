package com.wael.astimal.pos.core.data

import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.util.fetchAll
import com.wael.astimal.pos.core.util.pushAll
import com.wael.astimal.pos.features.inventory.data.remote.dto.CategoryDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.ProductDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.StockAdjustmentDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.StoreDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.UnitDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.toEntity
import com.wael.astimal.pos.features.inventory.domain.repository.CategoryRepository
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import com.wael.astimal.pos.features.management.data.remote.dto.BusinessPartnerDto
import com.wael.astimal.pos.features.management.data.remote.dto.EmployeeTransactionDto
import com.wael.astimal.pos.features.management.data.remote.dto.PartnerTransactionDto
import com.wael.astimal.pos.features.management.data.remote.dto.toEntity
import com.wael.astimal.pos.features.management.domain.entity.toDto
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.EmployeeTransactionRepository
import com.wael.astimal.pos.features.management.domain.repository.PartnerTransactionRepository
import com.wael.astimal.pos.features.user.data.remote.dto.ProfileDto
import com.wael.astimal.pos.features.user.data.remote.dto.toEntity
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest


class SyncServiceImpl(
    private val supabaseClient: SupabaseClient,
    private val syncManager: SyncManager,
    private val unitRepository: UnitRepository,
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository,
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository,
    private val businessPartnerRepository: BusinessPartnerRepository,
    private val partnerTransactionRepository: PartnerTransactionRepository,
    private val employeeTransactionRepository: EmployeeTransactionRepository,
    private val navigationController: NavigationController
) : SyncService {

    override suspend fun performFullSync(): Result<Unit> {
        return try {
            // TODO Remove
            userRepository.login(
                "admin@mail.com", "adminadmin"
            ).onSuccess {
                navigationController.navigate(
                    Destination.Dashboard,
                    popUpToRoute = Destination.Auth,
                )
            }.getOrThrow()


            userRepository.getCurrentUser() ?: throw Exception("User not authenticated")

            supabaseClient.fetchAll<ProfileDto>("profiles").getOrThrow().also {
                userRepository.syncWithServer(
                    it.map { profileDto -> profileDto.toEntity() },
                )
            }

            supabaseClient.fetchAll<StoreDto>("stores").getOrThrow().also {
                storeRepository.syncWithServer(
                    it.map { storeDto -> storeDto.toEntity() })
            }

            supabaseClient.fetchAll<CategoryDto>("categories").getOrThrow().also {
                categoryRepository.syncWithServer(
                    it.map { categoryDto -> categoryDto.toEntity() })
            }

            supabaseClient.fetchAll<UnitDto>("units").getOrThrow().also {
                unitRepository.syncWithServer(
                    it.map { unitDto -> unitDto.toEntity() })
            }

            supabaseClient.fetchAll<ProductDto>("products").getOrThrow().also {
                productRepository.syncWithServer(
                    it.map { unitDto ->
                        unitDto.toEntity(
                            categoryId = unitDto.categoryId?.let { id ->
                                categoryRepository.getCategoryByServerId(
                                    id
                                )
                            }?.getOrThrow()?.id?.local,
                            mainUnitId = unitRepository.getUnitByServerId(unitDto.mainUnitId)
                                .getOrThrow().id.local,
                            subUnitId = unitDto.subUnitId?.let { id ->
                                unitRepository.getUnitByServerId(
                                    id
                                )
                            }?.getOrThrow()?.id?.local
                        )
                    },
                )
            }

            supabaseClient.fetchAll<StockAdjustmentDto>("stock_adjustments").getOrThrow().also {
                stockRepository.syncWithServer(
                    it.map { stockAdjustmentDto ->
                        stockAdjustmentDto.toEntity(
                            storeId = storeRepository.getStoreBySeverId(
                                stockAdjustmentDto.storeId
                            ).getOrThrow().id.local,
                            productId = productRepository.getProductByServerId(
                                stockAdjustmentDto.productId
                            ).getOrThrow().id.local,
                            userId = userRepository.getUserByServerId(
                                stockAdjustmentDto.userId
                            ).getOrThrow()!!.id.local
                        )
                    },
                )
            }

            syncPartner()
            syncPartnerTransactions()
            syncEmployeesTransactions()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun syncPartner() {
        businessPartnerRepository.getAllUnSynced().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.let {
            println(it)
            supabaseClient.pushAll<BusinessPartnerDto>("business_partners") { it }
        }?.getOrThrow()

        businessPartnerRepository.getAllDeletedPartners().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.forEach {
            println(it)
            supabaseClient.postgrest["business_partners"].delete {
                filter {
                    eq("id", it.id)
                }
            }
            businessPartnerRepository.hardDeleteByServerId(it.id)
        }

        supabaseClient.fetchAll<BusinessPartnerDto>("business_partners").getOrThrow().also {
            businessPartnerRepository.syncWithServer(
                it.map { businessPartnerDto ->
                    businessPartnerDto.toEntity(
                        responsibleId = userRepository.getUserByServerId(
                            businessPartnerDto.responsibleId
                        ).getOrThrow()!!.id.local
                    )
                },
            )
        }
    }

    private suspend fun syncEmployeesTransactions() {
        employeeTransactionRepository.getUnsyncedTransactions().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<EmployeeTransactionDto>("employee_transactions") { it }
        }?.getOrThrow()

        employeeTransactionRepository.getAllDeletedTransactions().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.forEach {
            supabaseClient.postgrest["employee_transactions"].delete {
                filter {
                    eq("id", it.id)
                }
            }
            employeeTransactionRepository.hardDeleteByServerId(it.id)
        }

        supabaseClient.fetchAll<EmployeeTransactionDto>("employee_transactions").getOrThrow()
            .also {
                employeeTransactionRepository.syncWithServer(
                    it.map { employeeTransactionDto ->
                        employeeTransactionDto.toEntity(
                            employeeId = userRepository.getUserByServerId(
                                employeeTransactionDto.employeeId
                            ).getOrThrow()!!.id.local,
                            createdByEmployeeId = userRepository.getUserByServerId(
                                employeeTransactionDto.creatorId
                            ).getOrThrow()!!.id.local
                        )
                    },
                )
            }
    }

    private suspend fun syncPartnerTransactions() {
        partnerTransactionRepository.getUnsyncedTransactions().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<PartnerTransactionDto>("partner_transactions") { it }
        }?.getOrThrow()

        partnerTransactionRepository.getAllDeletedTransactions().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.forEach {
            supabaseClient.postgrest["partner_transactions"].delete {
                filter {
                    eq("id", it.id)
                }
            }
            partnerTransactionRepository.hardDeleteByServerId(it.id)
        }

        supabaseClient.fetchAll<PartnerTransactionDto>("partner_transactions").getOrThrow()
            .also {
                partnerTransactionRepository.syncWithServer(
                    it.map { partnerTransactionDto ->
                        partnerTransactionDto.toEntity(
                            partnerLocalId = businessPartnerRepository.getBusinessPartnerByServerId(
                                partnerTransactionDto.partnerId
                            ).getOrThrow()!!.localId,
                            userLocalId = userRepository.getUserByServerId(
                                partnerTransactionDto.createdByUserId
                            ).getOrThrow()!!.id.local
                        )
                    },
                )
            }
    }
}

interface SyncService {
    suspend fun performFullSync(): Result<Unit>
}
