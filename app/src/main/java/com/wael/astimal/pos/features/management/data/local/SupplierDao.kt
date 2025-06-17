package com.wael.astimal.pos.features.management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.management.data.entity.SupplierEntity
import com.wael.astimal.pos.features.management.data.entity.SupplierWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSupplier(supplier: SupplierEntity): Long

    @Query("SELECT * FROM suppliers WHERE localId = :id")
    suspend fun getSupplierById(id: Long): SupplierWithDetailsEntity?

    @Query("SELECT * FROM suppliers WHERE NOT isDeletedLocally AND (arName LIKE '%' || :query || '%' OR enName LIKE '%' || :query || '%') ORDER BY enName ASC")
    fun searchSuppliersFlow(query: String): Flow<List<SupplierWithDetailsEntity>>

    @Query("UPDATE suppliers SET indebtedness = indebtedness + :change WHERE localId = :supplierId")
    suspend fun adjustIndebtedness(supplierId: Long, change: Double)

    // Add this method to your SupplierDao interface
    @Query("UPDATE suppliers SET isDeletedLocally = 1, updatedAt = :timestamp WHERE localId = :localId")
    suspend fun softDeleteSupplier(localId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT indebtedness FROM suppliers WHERE localId = :supplierId")
    fun getIndebtednessFlow(supplierId: Long): Flow<Double?>
}