package com.wael.astimal.pos.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
    @SerialName("date")
    val lastSyncDate: String
)
