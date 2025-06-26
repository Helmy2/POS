package com.wael.astimal.pos.core.data.remote

import com.wael.astimal.pos.core.data.remote.dto.SyncApiResponse
import com.wael.astimal.pos.core.data.remote.dto.SyncRequest
import com.wael.astimal.pos.core.util.ApiRoutes
import com.wael.astimal.pos.features.inventory.data.remote.dto.CategorySyncData
import com.wael.astimal.pos.features.inventory.data.remote.dto.UnitSyncData
import com.wael.astimal.pos.features.management.data.remote.dto.ClientSyncData
import com.wael.astimal.pos.features.management.data.remote.dto.SupplierSyncData
import com.wael.astimal.pos.features.user.data.remote.dto.EmployeeSyncData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface SyncApiService {
    suspend fun syncUnits(request: SyncRequest): Result<SyncApiResponse<UnitSyncData>>
    suspend fun syncEmployees(request: SyncRequest): Result<SyncApiResponse<EmployeeSyncData>>
    suspend fun syncClients(request: SyncRequest): Result<SyncApiResponse<ClientSyncData>>
    suspend fun syncSuppliers(request: SyncRequest): Result<SyncApiResponse<SupplierSyncData>>
    suspend fun syncCategories(request: SyncRequest): Result<SyncApiResponse<CategorySyncData>>
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

    override suspend fun syncEmployees(request: SyncRequest): Result<SyncApiResponse<EmployeeSyncData>> {
        return try {
            val response = client.post(ApiRoutes.SYNC_EMPLOYEES) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun syncClients(request: SyncRequest): Result<SyncApiResponse<ClientSyncData>> {
        return try {
            val response =
                client.post(ApiRoutes.SYNC_CLIENTS) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            Result.success(response.body())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun syncSuppliers(request: SyncRequest): Result<SyncApiResponse<SupplierSyncData>> {
        return try {
            val response = client.post(ApiRoutes.SYNC_SUPPLIERS) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun syncCategories(request: SyncRequest): Result<SyncApiResponse<CategorySyncData>> {
        return try {
            val response = client.post(ApiRoutes.SYNC_CATEGORIES) {
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
