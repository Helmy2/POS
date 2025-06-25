package com.wael.astimal.pos.core.data.remote.dto


import com.wael.astimal.pos.features.inventory.data.remote.dto.UnitDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UnitSyncData(
    val units: List<UnitDto>,
    @SerialName("date")
    val nextSyncDate: String
)
