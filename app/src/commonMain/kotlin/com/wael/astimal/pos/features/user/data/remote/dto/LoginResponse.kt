package com.wael.astimal.pos.features.user.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val user: UserDto,
    val token: String
)
