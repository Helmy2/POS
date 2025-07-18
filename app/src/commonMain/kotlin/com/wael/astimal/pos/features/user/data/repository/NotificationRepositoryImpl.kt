package com.wael.astimal.pos.features.user.data.repository

import com.wael.astimal.pos.features.user.domain.repository.NotificationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class NotificationRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : NotificationRepository {

    override suspend fun saveFcmToken(token: String): Result<Unit> {
        return try {
            val currentUser = supabaseClient.auth.currentUserOrNull()
                ?: return Result.failure(Exception("User not authenticated."))

            // Update the 'fcm_token' column in the 'profiles' table for the current user.
            supabaseClient.postgrest["profiles"]
                .update(
                    buildJsonObject { put("fcm_token", token) }
                ) {
                    filter {
                        eq("id", currentUser.id)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
