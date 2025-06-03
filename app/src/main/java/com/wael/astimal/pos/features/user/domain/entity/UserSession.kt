package com.wael.astimal.pos.features.user.domain.entity


enum class UserRole {
    ADMIN, EMPLOYEE, UNKNOWN
}

data class UserSession(
    val userId: Int,
    val userName: String,
    val userEmail: String,
    val userRole: UserRole,
    val authToken: String,
) {

    val isLoggedIn: Boolean
        get() = userId != DEFAULT_USER_ID

    companion object {
        const val DEFAULT_USER_ID = -1

        fun default(): UserSession = UserSession(
            userId = DEFAULT_USER_ID,
            userName = "",
            userEmail = "",
            userRole = UserRole.UNKNOWN,
            authToken = "",
        )
    }
}