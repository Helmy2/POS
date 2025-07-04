package com.wael.astimal.pos.features.management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wael.astimal.pos.features.management.data.entity.BusinessPartnerEntity
import com.wael.astimal.pos.features.management.data.entity.BusinessPartnerWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessPartnerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(partners: List<BusinessPartnerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(partner: BusinessPartnerEntity): Long

    @Query("DELETE FROM business_partners")
    suspend fun clearAll()

    @Transaction
    suspend fun clearAndInsert(partners: List<BusinessPartnerEntity>) {
        clearAll()
        insertAll(partners)
    }

    @Transaction
    @Query("SELECT * FROM business_partners WHERE NOT isDeletedLocally AND (enName LIKE '%' || :query || '%' OR arName LIKE '%' || :query || '%')")
    fun searchPartnersFlow(query: String): Flow<List<BusinessPartnerWithDetailsEntity>>

    @Transaction
    @Query("SELECT * FROM business_partners WHERE localId = :localId LIMIT 1")
    suspend fun getPartnerByLocalId(localId: Long): BusinessPartnerWithDetailsEntity?

    @Query("SELECT * FROM business_partners WHERE serverId = :id LIMIT 1")
    suspend fun getPartnerBySeverId(id: Long): BusinessPartnerEntity?
}
