package com.wael.astimal.pos.core.data

import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.util.fetchAll
import com.wael.astimal.pos.core.util.pullDeletedRecords
import com.wael.astimal.pos.core.util.pushAll
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
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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

    private var realtimeJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun startRealtimeDeletionsListener() {
        // Ensure we don't have multiple listeners running
        stopRealtimeDeletionsListener()
        try {
            val channel = supabaseClient.channel("deleted_records_listener")

            val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "deleted_records"
            }

            realtimeJob = changeFlow.onEach { insertAction ->
                pullDeletedRecords()
            }.catch { e ->
                e.printStackTrace()
            }.launchIn(coroutineScope)

            coroutineScope.launch {
                channel.subscribe()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRealtimeDeletionsListener() {
        realtimeJob?.cancel()
        realtimeJob = null
    }

    suspend fun pullDeletedRecords() {
        supabaseClient.pullDeletedRecords(
            lastSyncTimestamp = syncManager.getLastDelSyncDate().first()
        ).onSuccess {
            syncManager.updateLastDeletedSyncDate(it.lastSyncTimestamp)
        }
    }

    override suspend fun performFullSync(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                pullDeletedRecords()

                supabaseClient.fetchAll<ProfileDto>("profiles").getOrThrow().also {
                    userRepository.syncWithServer(
                        it.map { profileDto -> profileDto.toEntity() },
                    )
                }

                supabaseClient.fetchAll<StoreDto>("stores").getOrThrow().also {
                    storeRepository.syncWithServer(
                        it.map { storeDto ->
                            storeDto.toEntity()
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
        supabaseClient.fetchAll<StockTransferItemDto>("stock_transfer_items").getOrThrow().also {
            stockTransferRepository.syncTransfersItems(
                it.map { item ->
                    item.toEntity()
                }
            )
        }
    }

    private suspend fun syncTransfer() {
        supabaseClient.fetchAll<StockTransferDto>("stock_transfers").getOrThrow().also {
            stockTransferRepository.syncTransfers(
                it.map { transfer ->
                    transfer.toEntity()
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
                    unitDto.toEntity()
                },
            )
        }
    }

    private suspend fun syncInvoiceItems() {
        invoiceRepository.getUnsyncedInvoicesItems().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<ItemDto>("invoice_items") { it }
        }?.getOrThrow()

        supabaseClient.fetchAll<ItemDto>("invoice_items").getOrThrow().also {
            invoiceRepository.syncInvoicesItems(
                it.map { invoiceItemDto ->
                    invoiceItemDto.toEntity()
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

        supabaseClient.fetchAll<InvoiceDto>("invoices").getOrThrow().also {
            invoiceRepository.syncInvoices(
                it.map { invoiceDto ->
                    invoiceDto.toEntity()
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

        supabaseClient.fetchAll<StockAdjustmentDto>("stock_adjustments").getOrThrow().also {
            stockRepository.syncWithServer(
                it.map { stockAdjustmentDto ->
                    stockAdjustmentDto.toEntity()
                },
            )
        }
    }

    private suspend fun syncPartner() {
        businessPartnerRepository.getAllUnSynced().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<BusinessPartnerDto>("business_partners") { it }
        }?.getOrThrow()


        supabaseClient.fetchAll<BusinessPartnerDto>("business_partners").getOrThrow().also {
            businessPartnerRepository.syncWithServer(
                it.map { businessPartnerDto ->
                    businessPartnerDto.toEntity()
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

        supabaseClient.fetchAll<EmployeeTransactionDto>("employee_transactions").getOrThrow()
            .also {
                employeeTransactionRepository.syncWithServer(
                    it.map { employeeTransactionDto ->
                        employeeTransactionDto.toEntity()
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

        supabaseClient.fetchAll<PartnerTransactionDto>("partner_transactions").getOrThrow()
            .also {
                partnerTransactionRepository.syncWithServer(
                    it.map { partnerTransactionDto ->
                        partnerTransactionDto.toEntity()
                    },
                )
            }
    }
}

interface SyncService {
    suspend fun performFullSync(): Result<Unit>
    fun startRealtimeDeletionsListener()
}
