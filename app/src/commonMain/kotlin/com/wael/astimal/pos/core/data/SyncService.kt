package com.wael.astimal.pos.core.data

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
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
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
) : SyncService {

    private var syncListenerJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        syncManager.syncFlow.onEach {
            pullDeletedRecords()
            syncAllDataWithServer()
        }.catch {
            it.printStackTrace()
        }.launchIn(coroutineScope)
    }

    override fun startRealtimeListener() {
        stopRealtimeListener()

        try {
            val channel = supabaseClient.channel("sync_records_listener")

            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public")

            syncListenerJob = changeFlow.onEach {
                pullDeletedRecords()
                syncAllDataWithServer()
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

    override fun stopRealtimeListener() {
        syncListenerJob?.cancel()
        syncListenerJob = null
    }

    suspend fun pullDeletedRecords() {
        supabaseClient.pullDeletedRecords(
            lastSyncTimestamp = syncManager.lastDeletedSyncDate()
        ).onSuccess { (deletedList, lastSyncTimestamp) ->
            val map = deletedList.groupBy { it.tableName }

            stockRepository.deleteAll(map["stock_adjustments"]?.map { it.recordId } ?: emptyList())
                .getOrThrow()
            partnerTransactionRepository.deleteAll(map["partner_transactions"]?.map { it.recordId }
                ?: emptyList()).getOrThrow()
            employeeTransactionRepository.deleteAll(map["employee_transactions"]?.map { it.recordId }
                ?: emptyList()).getOrThrow()


            stockTransferRepository.deleteAll(map["stock_transfers"]?.map { it.recordId }
                ?: emptyList(), map["stock_transfer_items"]?.map { it.recordId } ?: emptyList())
                .getOrThrow()
            invoiceRepository.deleteAll(map["invoices"]?.map { it.recordId } ?: emptyList(),
                map["invoice_items"]?.map { it.recordId } ?: emptyList()).getOrThrow()


            productRepository.deleteAll(map["products"]?.map { it.recordId } ?: emptyList())
                .getOrThrow()
            businessPartnerRepository.deleteAll(map["business_partners"]?.map { it.recordId }
                ?: emptyList()).getOrThrow()

            storeRepository.deleteAll(map["stores"]?.map { it.recordId } ?: emptyList())
                .getOrThrow()
            categoryRepository.deleteAll(map["categories"]?.map { it.recordId } ?: emptyList())
                .getOrThrow()
            unitRepository.deleteAll(map["units"]?.map { it.recordId } ?: emptyList()).getOrThrow()
            userRepository.deleteAll(map["profiles"]?.map { it.recordId } ?: emptyList())
                .getOrThrow()

            syncManager.updateLastDeletedSyncDate(lastSyncTimestamp)
        }
    }

    override suspend fun performSync(): Result<Unit> {
        return try {
            syncAllDataWithServer()
            pullDeletedRecords()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }


    private suspend fun syncAllDataWithServer() {
        return withContext(Dispatchers.IO) {
            try {
                val lastSyncDate = syncManager.lastSyncDate()
                val currentDateTime = getCurrentServerTime().getOrThrow()
                syncProfile(lastSyncDate)
                syncStore(lastSyncDate)
                syncCategory(lastSyncDate)
                syncUnits(lastSyncDate)
                syncProducts(lastSyncDate)
                syncPartner(lastSyncDate, currentDateTime)
                syncTransfer(lastSyncDate)
                syncTransferItems(lastSyncDate)
                syncInvoice(lastSyncDate, currentDateTime)
                syncInvoiceItems(lastSyncDate, currentDateTime)
                syncStockAdjustment(lastSyncDate, currentDateTime)
                syncPartnerTransactions(lastSyncDate, currentDateTime)
                syncEmployeesTransactions(lastSyncDate, currentDateTime)

                syncManager.updateLastSyncDate(currentDateTime)
                Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    private suspend fun syncProfile(lastSyncDate: String) {
        supabaseClient.fetchAll<ProfileDto>("profiles", lastSyncDate).getOrThrow().also {
            userRepository.syncWithServer(
                it.map { profileDto -> profileDto.toEntity() },
            )
        }
    }

    private suspend fun syncStore(lastSyncDate: String) {
        supabaseClient.fetchAll<StoreDto>("stores", lastSyncDate).getOrThrow().also {
            storeRepository.syncWithServer(
                it.map { storeDto ->
                    storeDto.toEntity()
                },
            )
        }
    }

    private suspend fun syncCategory(lastSyncDate: String) {
        supabaseClient.fetchAll<CategoryDto>("categories", lastSyncDate).getOrThrow().also {
            categoryRepository.syncWithServer(
                it.map { categoryDto -> categoryDto.toEntity() })
        }
    }

    private suspend fun syncUnits(lastSyncDate: String) {
        supabaseClient.fetchAll<UnitDto>("units", lastSyncDate).getOrThrow().also {
            unitRepository.syncWithServer(
                it.map { unitDto -> unitDto.toEntity() })
        }
    }

    private suspend fun syncTransferItems(lastSyncDate: String) {
        supabaseClient.fetchAll<StockTransferItemDto>("stock_transfer_items", lastSyncDate)
            .getOrThrow().also {
                stockTransferRepository.syncTransfersItems(
                    it.map { item ->
                        item.toEntity()
                    })
            }
    }

    private suspend fun syncTransfer(lastSyncDate: String) {
        supabaseClient.fetchAll<StockTransferDto>("stock_transfers", lastSyncDate).getOrThrow()
            .also {
                stockTransferRepository.syncTransfers(
                    it.map { transfer ->
                        transfer.toEntity()
                    },
                )
            }
    }

    private suspend fun syncProducts(lastSyncDate: String) {
        productRepository.getUnsyncedProducts().getOrThrow().map {
            it.toDto()
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<ProductDto>("products") { it }
        }

        supabaseClient.fetchAll<ProductDto>("products", lastSyncDate).getOrThrow().also {
            productRepository.syncWithServer(
                it.map { unitDto ->
                    unitDto.toEntity()
                },
            )
        }
    }

    private suspend fun syncInvoiceItems(lastSyncDate: String, currentDateTime: String) {
        invoiceRepository.getUnsyncedInvoicesItems().getOrThrow().map {
            it.toDto(currentDateTime)
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<ItemDto>("invoice_items") { it }
        }?.getOrThrow()

        supabaseClient.fetchAll<ItemDto>("invoice_items", lastSyncDate).getOrThrow().also {
            invoiceRepository.syncInvoicesItems(
                it.map { invoiceItemDto ->
                    invoiceItemDto.toEntity()
                },
            )
        }
    }

    private suspend fun syncInvoice(lastSyncDate: String, currentDateTime: String) {
        invoiceRepository.getUnsyncedInvoices().getOrThrow().map {
            it.toDto(currentDateTime)
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<InvoiceDto>("invoices") { it }
        }?.getOrThrow()

        supabaseClient.fetchAll<InvoiceDto>("invoices", lastSyncDate).getOrThrow().also {
            invoiceRepository.syncInvoices(
                it.map { invoiceDto ->
                    invoiceDto.toEntity()
                },
            )
        }
    }

    private suspend fun syncStockAdjustment(lastSyncDate: String, currentDateTime: String) {
        stockRepository.getAllUnSynced().getOrThrow().map {
            it.toDto(currentDateTime)
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<StockAdjustmentDto>("stock_adjustments") { it }
        }?.getOrThrow()

        supabaseClient.fetchAll<StockAdjustmentDto>("stock_adjustments", lastSyncDate).getOrThrow()
            .also {
                stockRepository.syncWithServer(
                    it.map { stockAdjustmentDto ->
                        stockAdjustmentDto.toEntity()
                    },
                )
            }
    }

    private suspend fun syncPartner(lastSyncDate: String, currentDateTime: String) {
        businessPartnerRepository.getAllUnSynced().getOrThrow().map {
            it.toDto(currentDateTime)
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<BusinessPartnerDto>("business_partners") { it }
        }?.getOrThrow()


        supabaseClient.fetchAll<BusinessPartnerDto>("business_partners", lastSyncDate).getOrThrow()
            .also {
                businessPartnerRepository.syncWithServer(
                    it.map { businessPartnerDto ->
                        businessPartnerDto.toEntity()
                    },
                )
            }
    }

    private suspend fun syncEmployeesTransactions(lastSyncDate: String, currentDateTime: String) {
        employeeTransactionRepository.getUnsyncedTransactions().getOrThrow().map {
            it.toDto(currentDateTime)
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<EmployeeTransactionDto>("employee_transactions") { it }
        }?.getOrThrow()

        supabaseClient.fetchAll<EmployeeTransactionDto>("employee_transactions", lastSyncDate)
            .getOrThrow().also {
                employeeTransactionRepository.syncWithServer(
                    it.map { employeeTransactionDto ->
                        employeeTransactionDto.toEntity()
                    },
                )
            }
    }

    private suspend fun syncPartnerTransactions(lastSyncDate: String, currentDateTime: String) {
        partnerTransactionRepository.getUnsyncedTransactions().getOrThrow().map {
            it.toDto(currentDateTime)
        }.takeIf { it.isNotEmpty() }?.let {
            supabaseClient.pushAll<PartnerTransactionDto>("partner_transactions") { it }
        }?.getOrThrow()

        supabaseClient.fetchAll<PartnerTransactionDto>("partner_transactions", lastSyncDate)
            .getOrThrow().also {
                partnerTransactionRepository.syncWithServer(
                    it.map { partnerTransactionDto ->
                        partnerTransactionDto.toEntity()
                    },
                )
            }
    }

    private suspend fun getCurrentServerTime(): Result<String> {
        return try {
            // Use postgrest.rpc to call the function and decode the raw string response.
            val serverTime = supabaseClient.postgrest.rpc(
                function = "get_server_timestamp"
            ).data

            // The response will be quoted (e.g., "\"2025-08-10T12:00:00+00:00\""),
            // so we remove the quotes.
            Result.success(serverTime.trim { it == '"' })
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

interface SyncService {
    suspend fun performSync(): Result<Unit>
    fun startRealtimeListener()
    fun stopRealtimeListener()
}
