package com.wael.astimal.pos.features.user.data.remote

import com.wael.astimal.pos.features.user.data.remote.dto.LoginRequest
import com.wael.astimal.pos.features.user.data.remote.dto.LoginResponse

interface AuthApiService {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
}
