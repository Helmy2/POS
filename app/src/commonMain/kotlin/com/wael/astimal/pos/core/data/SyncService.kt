package com.wael.astimal.pos.core.data

import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.util.fetchAll
import com.wael.astimal.pos.core.util.pushAll
import com.wael.astimal.pos.features.inventory.data.local.entity.toDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.CategoryDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.ProductDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.StockAdjustmentDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.StockTransferDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.StockTransferItemDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.StoreDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.UnitDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.toEntity
import com.wael.astimal.pos.features.inventory.domain.entity.toDto
import com.wael.astimal.pos.features.inventory.domain.repository.CategoryRepository
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockTransferRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import com.wael.astimal.pos.features.management.data.local.entity.toDto
import com.wael.astimal.pos.features.management.data.remote.dto.BusinessPartnerDto
import com.wael.astimal.pos.features.management.data.remote.dto.EmployeeTransactionDto
import com.wael.astimal.pos.features.management.data.remote.dto.InvoiceDto
import com.wael.astimal.pos.features.management.data.remote.dto.ItemDto
import com.wael.astimal.pos.features.management.data.remote.dto.PartnerTransactionDto
import com.wael.astimal.pos.features.management.data.remote.dto.toEntity
import com.wael.astimal.pos.features.management.domain.entity.toDto
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.EmployeeTransactionRepository
import com.wael.astimal.pos.features.management.domain.repository.InvoiceRepository
import com.wael.astimal.pos.features.management.domain.repository.PartnerTransactionRepository
import com.wael.astimal.pos.features.user.data.remote.dto.ProfileDto
import com.wael.astimal.pos.features.user.data.remote.dto.toEntity
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import toDto


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
    private val invoiceRepository: InvoiceRepository,
    private val stockTransferRepository: StockTransferRepository,
    private val navigationController: NavigationController
) : SyncService {

    override suspend fun performFullSync(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                supabaseClient.fetchAll<ProfileDto>("profiles").getOrThrow().also {
                    userRepository.syncWithServer(
                        it.map { profileDto -> profileDto.toEntity() },
                    )
                }

                supabaseClient.fetchAll<StoreDto>("stores").getOrThrow().also {
                    storeRepository.syncWithServer(
                        it.map { storeDto ->
                            storeDto.toEntity(
                                userRepository.getUserByServerId(storeDto.employeeId)
                                    .getOrThrow()!!.id.local
                            )
                        },
                    )
                }

                supabaseClient.fetchAll<CategoryDto>("categories").getOrThrow().also {
                    categoryRepository.syncWithServer(
                        it.map { categoryDto -> categoryDto.toEntity() })
                }

                supabaseClient.fetchAll<UnitDto>("units").getOrThrow().also {
                    unitRepository.syncWithServer(
                        it.map { unitDto -> unitDto.toEntity() })
                }

                syncProducts()

                syncPartner()
                syncTransfer()
                syncTransferItems()
                syncInvoice()
                syncInvoiceItems()
                syncStockAdjustment()
                syncPartnerTransactions()
                syncEmployeesTransactions()

                Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    private suspend fun syncTransferItems() {
        stockTransferRepository.getUnsyncedTransfersItems().getOrThrow().map {
            it.toDto(
                productId = productRepository.getProductByLocalId(it.productLocalId)
                    .getOrThrow().id.server!!
            )
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<StockTransferItemDto>("stock_transfer_items") { it }
        }

        // delete
        stockTransferRepository.getAllDeletedInvoiceItems().getOrThrow().map {
            it.toDto(
                productId = productRepository.getProductByLocalId(it.productLocalId)
                    .getOrThrow().id.server!!
            )
        }.takeIf { it.isNotEmpty() }?.forEach {
            supabaseClient.postgrest["stock_transfer_items"].delete {
                filter {
                    eq("id", it.id)
                }
            }
            stockTransferRepository.hardDeleteInvoiceItems(it.id)
        }

        supabaseClient.fetchAll<StockTransferItemDto>("stock_transfer_items").getOrThrow().also {
            stockTransferRepository.syncTransfersItems(
                it.map { item ->
                    item.toEntity(
                        productLocalId = productRepository.getProductByServerId(item.productId)
                            .getOrThrow().id.local
                    )
                }
            )
        }
    }

    private suspend fun syncTransfer() {
        stockTransferRepository.getUnsyncedTransfers().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<StockTransferDto>("stock_transfers") { it }
        }

        stockTransferRepository.getAllDeletedInvoice().getOrThrow().forEach {
            supabaseClient.postgrest["stock_transfers"].delete {
                filter {
                    eq("id", it.id)
                }
            }
            stockTransferRepository.hardDeleteInvoice(it.id)
        }

        supabaseClient.fetchAll<StockTransferDto>("stock_transfers").getOrThrow().also {
            stockTransferRepository.syncTransfers(
                it.map { transfer ->
                    transfer.toEntity(
                        fromStoreId = storeRepository.getStoreBySeverId(transfer.fromStoreId)
                            .getOrThrow().id.local,
                        toStoreId = storeRepository.getStoreBySeverId(transfer.toStoreId)
                            .getOrThrow().id.local,
                        initiatingUserId = userRepository.getUserByServerId(transfer.initiatingUserId)
                            .getOrThrow()!!.id.local,
                        receivingUserId = userRepository.getUserByServerId(transfer.receivingUserId)
                            .getOrThrow()!!.id.local
                    )
                },
            )
        }
    }

    private suspend fun syncProducts() {
        productRepository.getUnsyncedProducts().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<ProductDto>("products") { it }
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
    }

    private suspend fun syncInvoiceItems() {
        invoiceRepository.getUnsyncedInvoicesItems().getOrThrow().map {
            it.toDto(
                productId = productRepository.getProductByLocalId(
                    it.productId
                ).getOrThrow().id.server!!
            )
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<ItemDto>("invoice_items") { it }
        }?.getOrThrow()

        invoiceRepository.getAllDeletedInvoiceItems().getOrThrow().map {
            it.toDto(
                productId = productRepository.getProductByLocalId(
                    it.productId
                ).getOrThrow().id.server!!
            )
        }.takeIf { it.isNotEmpty() }?.forEach {
            supabaseClient.postgrest["invoice_items"].delete {
                filter {
                    eq("id", it.id)
                }
            }
            invoiceRepository.hardDeleteInvoiceItems(it.id)
        }

        supabaseClient.fetchAll<ItemDto>("invoice_items").getOrThrow().also {
            invoiceRepository.syncInvoicesItems(
                it.map { invoiceItemDto ->
                    invoiceItemDto.toEntity(
                        productId = productRepository.getProductByServerId(
                            invoiceItemDto.productId
                        ).getOrThrow().id.local,
                    )
                },
            )
        }
    }

    private suspend fun syncInvoice() {
        invoiceRepository.getUnsyncedInvoices().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<InvoiceDto>("invoices") { it }
        }?.getOrThrow()

        invoiceRepository.getAllDeletedInvoice().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.forEach {
            supabaseClient.postgrest["invoices"].delete {
                filter {
                    eq("id", it.id)
                }
            }
            invoiceRepository.hardDeleteInvoice(it.id)
        }

        supabaseClient.fetchAll<InvoiceDto>("invoices").getOrThrow().also {
            invoiceRepository.syncInvoices(
                it.map { invoiceDto ->
                    invoiceDto.toEntity(
                        partnerId = businessPartnerRepository.getBusinessPartnerByServerId(
                            invoiceDto.partnerId
                        ).getOrThrow()!!.localId,
                        employeeId = userRepository.getUserByServerId(invoiceDto.employeeId)
                            .getOrThrow()!!.id.local,
                        storeId = storeRepository.getStoreBySeverId(invoiceDto.storeId)
                            .getOrThrow().id.local
                    )
                },
            )
        }
    }

    private suspend fun syncStockAdjustment() {
        stockRepository.getAllUnSynced().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<StockAdjustmentDto>("stock_adjustments") { it }
        }?.getOrThrow()

        stockRepository.getAllDeleted().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.forEach {
            supabaseClient.postgrest["stock_adjustments"].delete {
                filter {
                    eq("id", it.id)
                }
            }
            stockRepository.hardDeleteByServerId(it.id)
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
