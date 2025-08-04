package com.wael.astimal.pos.core.data.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeletedRecord(
    val id: String,
    @SerialName("record_id")
    val recordId: String,
    @SerialName("table_name")
    val tableName: String,
    @SerialName("deleted_at")
    val deletedAt: String
)

@Serializable
data class DeletedRecordResponse(
    val deleted: List<DeletedRecord>,
    @SerialName("current_server_time")
    val lastSyncTimestamp: String
)