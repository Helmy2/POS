package com.wael.astimal.pos.features.user.domain.entity

data class UserSession(
    val userId: Int?,
    val userName: String?,
    val userEmail: String?,
    val userRole: String?,
    val authToken: String?,
)