package com.wael.astimal.pos.features.inventory.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: List<UnitEntity>)

    @Query("SELECT * FROM units WHERE arName LIKE '%' || :query || '%' OR enName LIKE '%' || :query || '%'")
    fun getAll(query: String): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE localId = :localId")
    suspend fun getByCode(localId: String): UnitEntity?

    @Query("SELECT * FROM units WHERE localId = :localId")
    suspend fun getByLocalId(localId: String): UnitEntity?

    @Query("SELECT * FROM units WHERE serverId = :serverId")
    suspend fun getByServerId(serverId: Int): UnitEntity?
}