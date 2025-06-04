
package com.wael.astimal.pos.features.inventory.domain.entity

data class StockTransfer(
    val localId: Long,
    val serverId: Int?,
    val fromStoreId: Long,
    val fromStoreName: String?,
    val toStoreId: Long,
    val toStoreName: String?,
    val initiatedByUserId: Long,
    val initiatedByUserName: String?,
    var isAccepted: Boolean?,
    val transferDate: Long,
    val items: List<StockTransferItem>,
    var isSynced: Boolean,
    var lastModified: Long,
    var isDeletedLocally: Boolean
)

data class StockTransferItem(
    val localId: Long,
    val serverId: Int?,
    val productLocalId: Long,
    val productArName: String?,
    val productEnName: String?,
    val unitLocalId: Long,
    val unitArName: String?,
    val unitEnName: String?,
    val quantity: Double,
    val maximumOpeningBalance: Double?,
    val minimumOpeningBalance: Double?
)

