package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.data.local.entity.StockTransferEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransferItem
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransferStatus
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.coroutines.flow.Flow

interface StockTransferRepository {

    suspend fun getPendingTransfersForApproval(): Flow<List<StockTransfer>>

    suspend fun setTransferApprovalStatus(transferId: String, approved: Boolean): Result<Unit>

    fun getStockTransfersWithDetails(): Flow<List<StockTransfer>>

    suspend fun deleteStockTransfer(transferToDelete: StockTransfer): Result<Unit>

    suspend fun updateStockTransfer(
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
    ): Result<Unit>

    suspend fun addStockTransfer(
        fromStore: Store,
        toStore: Store,
        initiatedByUser: User,
        items: List<StockTransferItem>,
        transferDate: Long,
        receivingUser: User,
        notes: String
    ): Result<Unit>

    suspend fun syncTransfersItems(entities: List<StockTransferItemEntity>): Result<Unit>
    suspend fun syncTransfers(entities: List<StockTransferEntity>): Result<Unit>
    suspend fun deleteAll(transferIds: List<String>, transferItemIds: List<String>): Result<Unit>
}