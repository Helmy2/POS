package com.wael.astimal.pos.features.client_management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.client_management.data.entity.SupplierEntity
import com.wael.astimal.pos.features.client_management.data.entity.SupplierWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSupplier(supplier: SupplierEntity): Long

    @androidx.room.Transaction
    @Query("SELECT * FROM suppliers WHERE NOT isDeletedLocally AND arName LIKE '%' || :query || '%' OR enName LIKE '%' || :query || '%'")
    fun searchSuppliersWithDetailsFlow(query: String): Flow<List<SupplierWithDetailsEntity>>
}