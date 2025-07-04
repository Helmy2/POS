package com.wael.astimal.pos.core.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncApiResponse<T>(
    val status: String,
    val message: String,
    val data: T
)