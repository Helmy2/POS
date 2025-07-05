package com.wael.astimal.pos.core.util

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

/**
 * A generic extension function on the SupabaseClient to fetch all records from a given table.
 * This is the primary method for the "pull" phase of our synchronization.
 *
 * This function is inline with a reified type parameter, allowing Ktor to automatically
 * infer the correct serializer for the DTO.
 *
 * @param T The DTO type to decode the JSON into (e.g., StoreDto, ProductDto).
 * @param tableName The name of the table in Supabase.
 * @return A Result containing the list of DTOs on success, or an exception on failure.
 */
suspend inline fun <reified T : Any> SupabaseClient.fetchAll(tableName: String): Result<List<T>> {
    return try {
        val result = this.postgrest[tableName]
            .select(columns = Columns.ALL)
            .decodeList<T>()
        Result.success(result)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}
