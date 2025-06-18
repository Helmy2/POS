package com.wael.astimal.pos.features.user.data.remote

import com.wael.astimal.pos.core.util.BASE_URL
import com.wael.astimal.pos.features.user.data.remote.dto.LoginRequest
import com.wael.astimal.pos.features.user.data.remote.dto.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiServiceImpl(
    private val client: HttpClient
) : AuthApiService {
    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = client.post("$BASE_URL/login") {
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
