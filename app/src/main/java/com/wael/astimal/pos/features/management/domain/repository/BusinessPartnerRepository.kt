package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.data.entity.BusinessPartnerEntity
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import kotlinx.coroutines.flow.Flow

interface BusinessPartnerRepository {
    fun getBusinessPartners(query: String = ""): Flow<List<BusinessPartner>>
    suspend fun saveBusinessPartner(partner: BusinessPartner): Result<Unit>
    suspend fun deleteBusinessPartner(partner: BusinessPartner): Result<Unit>
    fun getSuppliers(query: String = ""): Flow<List<BusinessPartner>>
    fun getClients(query: String = ""): Flow<List<BusinessPartner>>
    suspend fun getClient(clientId: Long): BusinessPartner?
    suspend fun syncWithServer(list: List<BusinessPartnerEntity>): Result<Unit>
    suspend fun getPartnerBalance(partner: BusinessPartner): Result<Double>
}
