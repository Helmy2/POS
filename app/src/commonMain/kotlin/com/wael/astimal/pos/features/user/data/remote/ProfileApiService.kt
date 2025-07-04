package com.wael.astimal.pos.features.user.data.remote

import com.wael.astimal.pos.features.user.data.remote.dto.ProfileDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

interface ProfileApiService {
    suspend fun getProfile(userId: String): Result<ProfileDto>
}

class ProfileApiServiceImpl(
    private val supabaseClient: SupabaseClient
) : ProfileApiService {
    override suspend fun getProfile(userId: String): Result<ProfileDto> {
        return try {
            val result = supabaseClient.postgrest["profiles"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", userId)
                    }
                }.decodeSingle<ProfileDto>()
            Result.success(result)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
