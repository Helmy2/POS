package com.wael.astimal.pos.features.inventory.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wael.astimal.pos.features.inventory.data.local.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StoreWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(store: StoreEntity): Long

    @Transaction
    @Query("SELECT * FROM stores WHERE localId = :localId LIMIT 1")
    suspend fun getStoreByLocalId(localId: String): StoreWithDetails?

    @Transaction
    @Query("SELECT * FROM stores WHERE NOT isDeletedLocally AND (arName LIKE '%' || :query || '%' OR enName LIKE '%' || :query || '%') ORDER BY arName ASC, enName ASC")
    fun searchStoresFlow(query: String): Flow<List<StoreWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(stores: List<StoreEntity>)

    @Transaction
    @Query("SELECT * FROM stores WHERE localId = :serverId LIMIT 1")
    suspend fun getStoreByServerId(serverId: String): StoreWithDetails?

    @Query("DELETE FROM stores WHERE localId = :localId")
    suspend fun deleteStoreByLocalId(localId: String)

    @Transaction
    @Query("SELECT * FROM stores WHERE employeeId = :local LIMIT 1")
    fun getStoreByUserId(local: String): Flow<StoreWithDetails?>

    @Query("DELETE FROM stores")
    suspend fun deleteAll()

}
