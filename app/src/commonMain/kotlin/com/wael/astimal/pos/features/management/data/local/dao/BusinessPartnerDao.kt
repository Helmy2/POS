package com.wael.astimal.pos.features.management.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wael.astimal.pos.features.management.data.local.entity.BusinessPartnerEntity
import com.wael.astimal.pos.features.management.data.local.entity.BusinessPartnerWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessPartnerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(partner: BusinessPartnerEntity): Long

    @Transaction
    @Query("SELECT * FROM business_partners WHERE NOT isDeletedLocally AND (enName LIKE '%' || :query || '%' OR arName LIKE '%' || :query || '%')")
    fun searchPartnersFlow(query: String): Flow<List<BusinessPartnerWithDetailsEntity>>

    @Transaction
    @Query("SELECT * FROM business_partners WHERE localId = :localId LIMIT 1")
    suspend fun getPartnerByLocalId(localId: Long): BusinessPartnerWithDetailsEntity?

    @Query("SELECT * FROM business_partners WHERE serverId = :id LIMIT 1")
    suspend fun getPartnerBySeverId(id: Long): BusinessPartnerEntity?

    @Query("DELETE FROM business_partners WHERE localId = :localId")
    suspend fun deletePartnerByLocalId(localId: Long)
}
