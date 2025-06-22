package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.entity.ClientEntity
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.SupplierEntity
import com.wael.astimal.pos.features.management.data.entity.TransactionType
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.ClientDao
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.SupplierDao
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class BusinessPartnerRepositoryImpl(
    private val db: AppDatabase,
    private val clientDao: ClientDao,
    private val supplierDao: SupplierDao,
    private val partnerTransactionDao: PartnerTransactionDao,
) : BusinessPartnerRepository {

    override fun getBusinessPartners(
        query: String
    ): Flow<List<BusinessPartner>> {
        val clientsFlow = clientDao.searchClientsWithDetailsFlow(query).map {
            it.map { it -> it.toDomain() }
        }
        val suppliersFlow = supplierDao.searchSuppliersFlow(query).map {
            it.map { it -> it.toDomain() }
        }

        return combine(clientsFlow, suppliersFlow) { clients, suppliers ->
            val partnerMap = mutableMapOf<String, BusinessPartner>()

            clients.forEach { client ->
                val mapKey =
                    (client.name.enName.orEmpty() + client.name.arName.orEmpty()).lowercase()
                partnerMap[mapKey] = client
            }

            suppliers.forEach { supplier ->
                val mapKey =
                    (supplier.name.enName.orEmpty() + supplier.name.arName.orEmpty()).lowercase()
                val existingPartner = partnerMap[mapKey]
                if (existingPartner != null) {
                    partnerMap[mapKey] = existingPartner.copy(
                        type = PartnerType.BOTH,
                        supplierLocalId = supplier.supplierLocalId,
                        supplierIndebtedness = supplier.supplierIndebtedness
                    )
                } else {
                    partnerMap[mapKey] = supplier
                }
            }
            partnerMap.values.toList().sortedBy { it.name.enName }
        }
    }

    override suspend fun saveBusinessPartner(partner: BusinessPartner): Result<Unit> {
        return try {
            db.withTransaction {
                val isNewClient =
                    partner.type != PartnerType.SUPPLIER && partner.clientLocalId == null
                val isNewSupplier =
                    partner.type != PartnerType.CLIENT && partner.supplierLocalId == null

                // Save Client record if applicable
                if (partner.type == PartnerType.CLIENT || partner.type == PartnerType.BOTH) {
                    val clientEntity =
                        partner.toClientEntity(isSupplier = partner.type == PartnerType.BOTH)
                    val clientId = clientDao.insertOrUpdateClient(clientEntity)
                    if (isNewClient) {
                        createOpeningBalanceTransaction(
                            clientId = clientId,
                            debit = partner.clientDebt,
                            credit = 0.0
                        )
                    }
                }

                // Save Supplier record if applicable
                if (partner.type == PartnerType.SUPPLIER || partner.type == PartnerType.BOTH) {
                    val supplierEntity =
                        partner.toSupplierEntity(isClient = partner.type == PartnerType.BOTH)
                    val supplierId = supplierDao.insertOrUpdateSupplier(supplierEntity)
                    if (isNewSupplier) {
                        createOpeningBalanceTransaction(
                            supplierId = supplierId,
                            debit = 0.0,
                            credit = partner.supplierIndebtedness
                        )
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBusinessPartner(partner: BusinessPartner): Result<Unit> {
        return try {
            db.withTransaction {
                val timestamp = System.currentTimeMillis()
                partner.clientLocalId?.let { clientDao.softDeleteClient(it.local, timestamp) }
                partner.supplierLocalId?.let { supplierDao.softDeleteSupplier(it.local, timestamp) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSuppliers(query: String): Flow<List<BusinessPartner>> {
        return getBusinessPartners(query).map {
            it.filter { it -> it.type != PartnerType.CLIENT }
        }
    }

    override suspend fun getSupplier(localId: Long): BusinessPartner? {
        return supplierDao.getSupplierById(localId)?.toDomain()
    }


    override fun searchClients(query: String): Flow<List<BusinessPartner>> {
        return getBusinessPartners(query).map {
            it.filter { it -> it.type != PartnerType.SUPPLIER }
        }
    }

    override suspend fun getClient(clientId: Long): BusinessPartner? {
        return clientDao.getClientWithDetails(clientId)?.toDomain()
    }


    private suspend fun createOpeningBalanceTransaction(
        clientId: Long? = null,
        supplierId: Long? = null,
        debit: Double,
        credit: Double,
    ) {
        if (debit == 0.0 && credit == 0.0) return

        partnerTransactionDao.insertTransaction(
            PartnerTransactionEntity(
                serverId = null,
                clientId = clientId,
                supplierId = supplierId,
                sourceTransactionId = 0L,
                transactionType = TransactionType.OPENING_BALANCE,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                debit = debit,
                credit = credit,
            )
        )
    }
}

private fun BusinessPartner.toClientEntity(isSupplier: Boolean): ClientEntity {
    return ClientEntity(
        serverId = null,
        localId = this.clientLocalId?.local ?: 0L,
        arName = this.name.arName.orEmpty(),
        enName = this.name.enName.orEmpty(),
        phone = this.phone,
        address = this.address,
        debt = this.clientDebt,
        isSupplier = isSupplier,
        responsibleEmployeeLocalId = this.responsibleEmployee.id,
        isSynced = false
    )
}

private fun BusinessPartner.toSupplierEntity(isClient: Boolean): SupplierEntity {
    return SupplierEntity(
        serverId = null,
        localId = this.supplierLocalId?.local ?: 0L,
        arName = this.name.arName.orEmpty(),
        enName = this.name.enName.orEmpty(),
        phone = this.phone,
        address = this.address,
        indebtedness = this.supplierIndebtedness,
        isClient = isClient,
        responsibleEmployeeLocalId = this.responsibleEmployee.id,
        isSynced = false
    )
}