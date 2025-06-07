package com.wael.astimal.pos.features.user.domain.entity



data class UserSession(
    val user: User? = null,
    val userId: Int,
    val authToken: String,
) {
    companion object {
        const val DEFAULT_USER_ID = -1
    }
}