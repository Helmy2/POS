package com.wael.astimal.pos.core.data.remote

import com.wael.astimal.pos.core.data.remote.dto.SyncApiResponse
import com.wael.astimal.pos.core.data.remote.dto.SyncRequest
import com.wael.astimal.pos.core.data.remote.dto.UnitSyncData
import com.wael.astimal.pos.core.util.ApiRoutes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface SyncApiService {
    suspend fun syncUnits(request: SyncRequest): Result<SyncApiResponse<UnitSyncData>>
}

class SyncApiServiceImpl(
    private val client: HttpClient,
) : SyncApiService {

    override suspend fun syncUnits(request: SyncRequest): Result<SyncApiResponse<UnitSyncData>> {
        return try {
            val response = client.post(ApiRoutes.SYNC_UNITS) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
