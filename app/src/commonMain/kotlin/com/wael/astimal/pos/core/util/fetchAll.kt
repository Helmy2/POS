package com.wael.astimal.pos.core.util

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns


/**
 * Fetches all records from a specified Supabase table and decodes them into a list of objects of type T.
 *
 * This is an extension function for [SupabaseClient].
 *
 * @param T The type of the objects to decode the records into. Must be a non-nullable type.
 * @param tableName The name of the table to fetch records from.
 * @return A [Result] object. If the operation is successful, it contains a [List] of objects of type T.
 *         If an error occurs, it contains the [Exception] that was thrown.
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


/**
 * Pushes a list of data to the specified table in Supabase.
 *
 * @param T The type of data to push.
 * @param tableName The name of the table to push data to.
 * @param data A lambda function that returns a list of data to push.
 * @return A [Result] object containing the list of pushed data if successful, or an exception if an error occurs.
 */
suspend inline fun <reified T : Any> SupabaseClient.pushAll(
    tableName: String,
    data: () -> List<T>
): Result<Unit> {
    return try {
        this.postgrest[tableName]
            .upsert(data())
        Result.success(Unit)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}