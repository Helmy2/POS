package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.entity.ClientEntity
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.SupplierEntity
import com.wael.astimal.pos.features.management.data.local.ClientDao
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.SupplierDao
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.Client
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.management.domain.entity.Supplier
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.management.domain.repository.SupplierRepository
import com.wael.astimal.pos.features.reports.domain.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class BusinessPartnerRepositoryImpl(
    private val db: AppDatabase,
    private val clientDao: ClientDao,
    private val supplierDao: SupplierDao,
    private val partnerTransactionDao: PartnerTransactionDao,
    private val clientRepository: ClientRepository,
    private val supplierRepository: SupplierRepository
) : BusinessPartnerRepository {

    override fun getBusinessPartners(query: String): Flow<List<BusinessPartner>> {
        val clientsFlow = clientRepository.searchClients(query)
        val suppliersFlow = supplierRepository.getSuppliers(query)

        return combine(clientsFlow, suppliersFlow) { clients, suppliers ->
            val partnerMap = mutableMapOf<String, BusinessPartner>()

            clients.forEach { client ->
                val mapKey =
                    (client.name.enName.orEmpty() + client.name.arName.orEmpty()).lowercase()
                partnerMap[mapKey] = client.toBusinessPartner(
                    partnerTransactionDao.getClientBalance(client.id) ?: 0.0
                )
            }

            suppliers.forEach { supplier ->
                val mapKey =
                    (supplier.name.enName.orEmpty() + supplier.name.arName.orEmpty()).lowercase()
                val existingPartner = partnerMap[mapKey]
                if (existingPartner != null) {
                    partnerMap[mapKey] = existingPartner.copy(
                        type = PartnerType.BOTH,
                        supplierLocalId = supplier.id,
                        supplierIndebtedness = partnerTransactionDao.getSupplierBalance(supplier.id)
                            ?: 0.0,
                    )
                } else {
                    partnerMap[mapKey] = supplier.toBusinessPartner(
                        partnerTransactionDao.getSupplierBalance(supplier.id) ?: 0.0
                    )
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
                partner.clientLocalId?.let { clientDao.softDeleteClient(it, timestamp) }
                partner.supplierLocalId?.let { supplierDao.softDeleteSupplier(it, timestamp) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
                clientId = clientId,
                supplierId = supplierId,
                sourceTransactionId = 0L, // 0 for opening balance as it has no source
                transactionType = TransactionType.OPENING_BALANCE,
                date = System.currentTimeMillis(),
                debit = debit,
                credit = credit,
            )
        )
    }
}

// Helper mapper functions
private fun Client.toBusinessPartner(
    clientDebt: Double
): BusinessPartner {
    return BusinessPartner(
        clientLocalId = this.id,
        supplierLocalId = null,
        name = this.name,
        address = this.address,
        phone = this.phone,
        responsibleEmployee = this.responsibleEmployee,
        type = PartnerType.CLIENT,
        clientDebt = clientDebt,
        supplierIndebtedness = 0.0,
        isSynced = this.isSynced
    )
}

private fun Supplier.toBusinessPartner(
    supplierIndebtedness: Double
): BusinessPartner {
    return BusinessPartner(
        clientLocalId = null,
        supplierLocalId = this.id,
        name = this.name,
        address = this.address,
        phone = this.phone,
        responsibleEmployee = this.responsibleEmployee,
        type = PartnerType.SUPPLIER,
        clientDebt = 0.0,
        supplierIndebtedness = supplierIndebtedness,
        isSynced = this.isSynced
    )
}

private fun BusinessPartner.toClientEntity(isSupplier: Boolean): ClientEntity {
    return ClientEntity(
        localId = this.clientLocalId ?: 0L,
        arName = this.name.arName.orEmpty(),
        enName = this.name.enName.orEmpty(),
        phone = this.phone,
        address = this.address,
        debt = this.clientDebt,
        isSupplier = isSupplier,
        responsibleEmployeeLocalId = this.responsibleEmployee?.id,
        isSynced = false
    )
}

private fun BusinessPartner.toSupplierEntity(isClient: Boolean): SupplierEntity {
    return SupplierEntity(
        localId = this.supplierLocalId ?: 0L,
        arName = this.name.arName.orEmpty(),
        enName = this.name.enName.orEmpty(),
        phone = this.phone,
        address = this.address,
        indebtedness = this.supplierIndebtedness,
        isClient = isClient,
        responsibleEmployeeLocalId = this.responsibleEmployee?.id,
        isSynced = false
    )
}