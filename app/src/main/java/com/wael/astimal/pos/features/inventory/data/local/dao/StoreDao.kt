package com.wael.astimal.pos.features.inventory.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(store: StoreEntity): Long

    @Query("SELECT * FROM stores WHERE localId = :localId LIMIT 1")
    suspend fun getStoreByLocalId(localId: Long): StoreEntity?

    @Query("SELECT * FROM stores WHERE NOT isDeletedLocally AND (arName LIKE '%' || :query || '%' OR enName LIKE '%' || :query || '%') ORDER BY arName ASC, enName ASC")
    fun searchStoresFlow(query: String): Flow<List<StoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(stores: List<StoreEntity>)

    @Query("SELECT * FROM stores WHERE serverId = :serverId LIMIT 1")
    suspend fun getStoreByServerId(serverId: Long): StoreEntity?

}
