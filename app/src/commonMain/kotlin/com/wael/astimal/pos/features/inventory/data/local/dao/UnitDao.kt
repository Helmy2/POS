package com.wael.astimal.pos.features.inventory.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.inventory.data.local.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: UnitEntity): Long

    @Query("SELECT * FROM units WHERE arName LIKE '%' || :query || '%' OR enName LIKE '%' || :query || '%'")
    fun getAll(query: String): Flow<List<UnitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(units: List<UnitEntity>)

    @Query("SELECT * FROM units WHERE localId = :serverId LIMIT 1")
    suspend fun getUnitByServerId(serverId: String): UnitEntity?

    @Query("DELETE FROM units WHERE localId = :localId")
    suspend fun deleteUnitByLocalId(localId: String)

    @Query("UPDATE units SET isDeletedLocally = 1")
    suspend fun deleteAll()
}