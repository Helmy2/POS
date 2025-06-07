
package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.features.user.data.entity.UserEntity

data class StockTransfer(
    val localId: Long,
    val serverId: Int?,
    val fromStore: Store?,
    val toStore: Store?,
    val initiatedByUser: UserEntity?,
    val transferDate: Long,
    val items: List<StockTransferItem>,
    var isSynced: Boolean,
    var lastModified: Long,
    var isDeletedLocally: Boolean
)

data class StockTransferItem(
    val localId: Long,
    val serverId: Int?,
    val product: Product?,
    val unit: Unit?,
    val quantity: Double,
    val maximumOpeningBalance: Double?,
    val minimumOpeningBalance: Double?
)

