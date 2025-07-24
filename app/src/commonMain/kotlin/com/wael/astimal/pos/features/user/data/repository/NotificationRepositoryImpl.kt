package com.wael.astimal.pos.features.user.data.repository

import com.wael.astimal.pos.features.user.data.local.SettingsManager
import com.wael.astimal.pos.features.user.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    private val settingsManager: SettingsManager,
) : NotificationRepository {

    override suspend fun saveFcmToken(token: String): Result<Unit> {
        return try {
            settingsManager.saveFcmToken(token)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
