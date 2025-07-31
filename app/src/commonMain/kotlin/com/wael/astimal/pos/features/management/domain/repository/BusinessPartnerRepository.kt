package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.data.local.entity.BusinessPartnerEntity
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import kotlinx.coroutines.flow.Flow

interface BusinessPartnerRepository {
    fun getBusinessPartners(query: String = ""): Flow<List<BusinessPartner>>
    suspend fun saveBusinessPartner(partner: BusinessPartner): Result<String>
    suspend fun deleteBusinessPartner(partner: BusinessPartner): Result<Unit>
    fun getSuppliers(query: String = ""): Flow<List<BusinessPartner>>
    fun getClients(query: String = ""): Flow<List<BusinessPartner>>
    suspend fun getClient(clientId: String): BusinessPartner?
    suspend fun syncWithServer(list: List<BusinessPartnerEntity>): Result<Unit>
    suspend fun getPartnerBalance(partner: BusinessPartner): Result<Double>
    suspend fun getBusinessPartnerByServerId(serverId: String): Result<BusinessPartnerEntity?>
    suspend fun hardDeleteByServerId(serverId: String): Result<Unit>
    suspend fun getAllUnSynced(): Result<List<BusinessPartner>>
    suspend fun getAllDeletedPartners(): Result<List<BusinessPartner>>
}
