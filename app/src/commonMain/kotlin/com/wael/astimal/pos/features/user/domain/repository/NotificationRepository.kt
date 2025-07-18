package com.wael.astimal.pos.features.user.domain.repository


/**
 * A repository for managing push notification tokens.
 */
interface NotificationRepository {
    /**
     * Saves the user's FCM push notification token to their Supabase profile.
     * @param token The FCM token for the current device.
     */
    suspend fun saveFcmToken(token: String): Result<Unit>
}
