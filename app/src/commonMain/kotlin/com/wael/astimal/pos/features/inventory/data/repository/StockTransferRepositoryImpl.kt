package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.deleteRecordAndLog
import com.wael.astimal.pos.core.util.toISOString
import com.wael.astimal.pos.features.inventory.data.local.dao.StockAdjustmentDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StockTransferDao
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StockTransferEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.remote.dto.StockTransferDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.StockTransferItemDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.toEntity
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransferItem
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransferStatus
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.inventory.domain.repository.StockTransferRepository
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class StockTransferRepositoryImpl(
    val supabaseClient: SupabaseClient,
    val stockTransferDao: StockTransferDao,
    val userRepository: UserRepository,
    val stockAdjustmentDao: StockAdjustmentDao,
) : StockTransferRepository {

    @OptIn(SupabaseExperimental::class)
    override suspend fun getPendingTransfersForApproval(): Flow<List<StockTransfer>> {
        val currentUserId = userRepository.getCurrentUser()?.id ?: return emptyFlow()

        return stockTransferDao.getPendingTransfersForApproval(
            currentUserId = currentUserId,
            status = StockTransferStatus.PENDING
        ).map { it -> it.map { it.toDomain() } }
    }

    override suspend fun setTransferApprovalStatus(
        transferId: String, approved: Boolean
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val status = if (approved) "approved" else "rejected"
                supabaseClient.postgrest["stock_transfers"].update(
                    buildJsonObject {
                        put("status", status)
                    }
                ) {
                    filter {
                        eq("id", transferId)
                    }
                }

                stockTransferDao.setTransferApprovalStatus(
                    transferId,
                    if (approved) StockTransferStatus.APPROVED else StockTransferStatus.REJECTED
                )

                if (approved) {
                    val items = supabaseClient.postgrest["stock_transfer_items"]
                        .select {
                            filter {
                                eq("transfer_id", transferId)
                            }
                        }.decodeList<StockTransferItemDto>().map { it.toEntity() }

                    val transfer = supabaseClient.postgrest["stock_transfers"]
                        .select {
                            filter {
                                eq("id", transferId)
                            }
                        }.decodeSingle<StockTransferDto>().toEntity()

                    stockTransferDao.insertStockTransferItems(items)
                    stockTransferDao.insertStockOrUpdateTransfer(transfer)

                    val newTransfer =
                        stockTransferDao.getStockTransfer(transferId).toDomain()
                    createAdjustStock(newTransfer)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun createAdjustStock(
        transfer: StockTransfer
    ) {
        for (item in transfer.items) {
            stockAdjustmentDao.insert(
                StockAdjustmentEntity(
                    localId = Uuid.random().toString(),
                    productId = item.product.id,
                    quantityChange = -item.quantity,
                    createdAt = Clock.now(),
                    updatedAt = Clock.now(),
                    userId = transfer.initiatingUser.id,
                    reason = StockAdjustmentReason.INVOICE,
                    storeId = transfer.fromStore.id,
                    invoiceId = null,
                    transactionId = transfer.id,
                    isSynced = false,
                    notes = null,
                )
            )
            stockAdjustmentDao.insert(
                StockAdjustmentEntity(
                    localId = Uuid.random().toString(),
                    productId = item.product.id,
                    quantityChange = item.quantity,
                    createdAt = Clock.now(),
                    updatedAt = Clock.now(),
                    userId = transfer.initiatingUser.id,
                    reason = StockAdjustmentReason.INVOICE,
                    storeId = transfer.toStore.id,
                    invoiceId = null,
                    transactionId = transfer.id,
                    isSynced = false,
                    notes = null,
                )
            )
        }
    }

    override fun getStockTransfersWithDetails(): Flow<List<StockTransfer>> {
        return stockTransferDao.getAllStockTransfersWithDetailsFlow()
            .map { it -> it.map { it.toDomain() } }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun updateStockTransfer(
        transferLocalId: String,
        fromStore: Store,
        toStore: Store,
        initiatedByUser: User,
        items: List<StockTransferItem>,
        transferDate: Long,
        receivingUser: User,
        notes: String,
        status: StockTransferStatus,
        createdAt: Long
    ): Result<Unit> {
        return try {
            if (status == StockTransferStatus.APPROVED) {
                return Result.failure(Exception("Cannot update an approved transfer"))
            }
            if (status == StockTransferStatus.REJECTED) {
                return Result.failure(Exception("Cannot update a rejected transfer"))
            }
            val transferDto = StockTransferDto(
                id = transferLocalId,
                fromStoreId = fromStore.id,
                toStoreId = toStore.id,
                initiatingUserId = initiatedByUser.id,
                receivingUserId = receivingUser.id,
                notes = notes,
                status = "pending",
                createdAt = createdAt.toISOString(),
                updatedAt = Clock.now().toISOString()
            )

            supabaseClient.postgrest["stock_transfers"]
                .update(transferDto) {
                    filter {
                        eq("id", transferLocalId)
                    }
                }

            supabaseClient.from("stock_transfer_items").delete {
                filter {
                    eq("transfer_id", transferLocalId)
                }
            }

            val itemDtos = items.map {
                StockTransferItemDto(
                    id = Uuid.random().toString(),
                    transferId = transferLocalId,
                    productId = it.product.id,
                    quantity = it.quantity,
                )
            }

            supabaseClient.postgrest["stock_transfer_items"].insert(itemDtos)

            stockTransferDao.updateTransferWithItems(
                transfer = transferDto.toEntity(),
                items = itemDtos.map {
                    it.toEntity()
                }
            )

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addStockTransfer(
        fromStore: Store,
        toStore: Store,
        initiatedByUser: User,
        items: List<StockTransferItem>,
        transferDate: Long,
        receivingUser: User,
        notes: String
    ): Result<Unit> {
        return try {
            val transferDto = StockTransferDto(
                id = Uuid.random().toString(),
                fromStoreId = fromStore.id,
                toStoreId = toStore.id,
                initiatingUserId = initiatedByUser.id,
                receivingUserId = receivingUser.id,
                notes = notes,
                status = "pending",
                createdAt = Clock.now().toISOString(),
                updatedAt = Clock.now().toISOString()
            )
            supabaseClient.postgrest["stock_transfers"]
                .insert(transferDto)


            val itemDtos = items.map {
                StockTransferItemDto(
                    id = Uuid.random().toString(),
                    transferId = transferDto.id,
                    productId = it.product.id,
                    quantity = it.quantity,
                )
            }

            supabaseClient.postgrest["stock_transfer_items"].insert(itemDtos)

            stockTransferDao.insertTransferWithItems(
                transferDto.toEntity(),
                itemDtos.map {
                    it.toEntity()
                },
            )

            receivingUser.fcmToken?.let {
                sendPushNotification(
                    notificationTitle = "New transfer from ${fromStore.name.enName} needs your approval.",
                    notificationBody = "Tap to open the app.",
                    recipientFcmToken = it
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteStockTransfer(transferToDelete: StockTransfer): Result<Unit> {
        if (transferToDelete.status == StockTransferStatus.APPROVED) {
            return Result.failure(Exception("Cannot delete an approved transfer"))
        }
        return try {
            stockTransferDao.getItemsForTransfer(transferToDelete.id).forEach {
                supabaseClient.deleteRecordAndLog(
                    targetTableName = "stock_transfer_items", targetRecordId = it.localId
                ).getOrThrow()
            }

            stockAdjustmentDao.getAdjustmentsByTransferId(transferToDelete.id).forEach {
                supabaseClient.deleteRecordAndLog(
                    targetTableName = "stock_adjustments", targetRecordId = it.localId
                ).getOrThrow()
            }

            supabaseClient.deleteRecordAndLog(
                targetTableName = "stock_transfers", targetRecordId = transferToDelete.id
            ).getOrThrow()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun syncTransfers(entities: List<StockTransferEntity>): Result<Unit> {
        return runCatching {
            entities.forEach {
                stockTransferDao.insertStockOrUpdateTransfer(it)
            }
        }
    }

    override suspend fun syncTransfersItems(entities: List<StockTransferItemEntity>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                stockTransferDao.deleteAllStockTransferItems()
                entities.forEach {
                    stockTransferDao.insertStockOrUpdateTransferItem(it)
                }
            }
        }
    }


    private suspend fun sendPushNotification(
        recipientFcmToken: String,
        notificationTitle: String,
        notificationBody: String
    ) {
        try {
            // 1. Create the JSON payload to send to the function
            // The keys ("token", "title", "body") MUST match what your Edge Function expects
            val payload = buildJsonObject {
                put("token", recipientFcmToken)
                put("title", notificationTitle)
                put("body", notificationBody)
            }

            // 2. Invoke the function by its name
            println("Invoking function 'send-transfer-notification'...")
            supabaseClient.functions.invoke(
                function = "send-transfer-notification", body = payload
            )

            // 3. Handle the success case
            println("Function invoked successfully! FCM should be sending the notification.")

        } catch (e: Exception) {
            // 4. Handle any errors
            e.printStackTrace()
            println("Error invoking function: ${e.message}")
        }
    }

    override suspend fun deleteAll(
        transferIds: List<String>,
        transferItemIds: List<String>
    ): Result<Unit> {
        return try {
            transferItemIds.forEach { stockTransferDao.hardDeleteStockTransferItem(it) }
            transferIds.forEach { stockTransferDao.hardDeleteStockTransfer(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}