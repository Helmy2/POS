package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.entity.ClientEntity
import com.wael.astimal.pos.features.management.data.entity.SupplierEntity
import com.wael.astimal.pos.features.management.data.local.ClientDao
import com.wael.astimal.pos.features.management.data.local.SupplierDao
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.management.domain.repository.SupplierRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * An implementation of the BusinessPartnerRepository that acts as an adapter
 * over the Client and Supplier data sources.
 *
 * @param db The Room database instance, used for running atomic transactions.
 * @param clientDao The Data Access Object for clients.
 * @param supplierDao The Data Access Object for suppliers.
 * @param clientRepository The repository for read-only client operations.
 * @param supplierRepository The repository for read-only supplier operations.
 */
class BusinessPartnerRepositoryImpl(
    private val db: AppDatabase,
    private val clientDao: ClientDao,
    private val supplierDao: SupplierDao,
    private val clientRepository: ClientRepository, // Kept for getBusinessPartners
    private val supplierRepository: SupplierRepository // Kept for getBusinessPartners
) : BusinessPartnerRepository {

    override fun getBusinessPartners(query: String): Flow<List<BusinessPartner>> {
        val clientsFlow = clientRepository.searchClients(query)
        val suppliersFlow = supplierRepository.getSuppliers(query)

        return combine(clientsFlow, suppliersFlow) { clients, suppliers ->
            val partnerMap = mutableMapOf<String, BusinessPartner>()

            clients.forEach { client ->
                val partner = BusinessPartner(
                    clientLocalId = client.id,
                    supplierLocalId = null,
                    name = client.name,
                    address = client.address,
                    phones = client.phones,
                    responsibleEmployee = client.responsibleEmployee,
                    type = PartnerType.CLIENT,
                    clientDebt = client.debt ?: 0.0,
                    isSynced = client.isSynced
                )
                // Use a combination of English and Arabic names for a more robust key
                val mapKey =
                    (client.name.enName.orEmpty() + client.name.arName.orEmpty()).lowercase()
                partnerMap[mapKey] = partner
            }

            suppliers.forEach { supplier ->
                val mapKey =
                    (supplier.name.enName.orEmpty() + supplier.name.arName.orEmpty()).lowercase()
                val existingPartner = partnerMap[mapKey]
                if (existingPartner != null) {
                    // This entity is both a client and a supplier
                    partnerMap[mapKey] = existingPartner.copy(
                        type = PartnerType.BOTH,
                        supplierLocalId = supplier.id,
                        supplierIndebtedness = supplier.indebtedness ?: 0.0
                    )
                } else {
                    // This entity is only a supplier
                    val partner = BusinessPartner(
                        clientLocalId = null,
                        supplierLocalId = supplier.id,
                        name = supplier.name,
                        address = supplier.address,
                        phones = supplier.phones,
                        responsibleEmployee = supplier.responsibleEmployee,
                        type = PartnerType.SUPPLIER,
                        supplierIndebtedness = supplier.indebtedness ?: 0.0,
                        isSynced = supplier.isSynced
                    )
                    partnerMap[mapKey] = partner
                }
            }
            partnerMap.values.toList().sortedBy { it.name.enName }
        }
    }

    /**
     * Saves a BusinessPartner. This operation is transactional.
     * It handles creating or updating records in the clients and/or suppliers table
     * based on the partner's type.
     */
    override suspend fun saveBusinessPartner(partner: BusinessPartner): Result<Unit> {
        return try {
            db.withTransaction {
                when (partner.type) {
                    PartnerType.CLIENT -> {
                        // Save to clients table, ensuring the supplier flag is false
                        clientDao.insertOrUpdateClient(partner.toClientEntity(isSupplier = false))
                    }

                    PartnerType.SUPPLIER -> {
                        // Save to suppliers table, ensuring the client flag is false
                        supplierDao.insertOrUpdateSupplier(partner.toSupplierEntity(isClient = false))
                    }

                    PartnerType.BOTH -> {
                        // Save to both tables, ensuring the flags are true
                        clientDao.insertOrUpdateClient(partner.toClientEntity(isSupplier = true))
                        supplierDao.insertOrUpdateSupplier(partner.toSupplierEntity(isClient = true))
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // In a real app, you would log the exception e
            Result.failure(e)
        }
    }

    /**
     * Deletes a BusinessPartner. This is a soft delete and is transactional.
     * It marks the corresponding records in the clients and/or suppliers table as deleted.
     */
    override suspend fun deleteBusinessPartner(partner: BusinessPartner): Result<Unit> {
        return try {
            db.withTransaction {
                val timestamp = System.currentTimeMillis()
                // If a client record exists, soft-delete it
                partner.clientLocalId?.let {
                    clientDao.softDeleteClient(it, timestamp)
                }
                // If a supplier record exists, soft-delete it
                partner.supplierLocalId?.let {
                    supplierDao.softDeleteSupplier(it, timestamp)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // In a real app, you would log the exception e
            Result.failure(e)
        }
    }
}


private fun BusinessPartner.toClientEntity(isSupplier: Boolean): ClientEntity {
    return ClientEntity(
        localId = this.clientLocalId ?: 0L,
        arName = this.name.arName.orEmpty(),
        enName = this.name.enName.orEmpty(),
        phone1 = this.phones.getOrNull(0),
        phone2 = this.phones.getOrNull(1),
        phone3 = this.phones.getOrNull(2),
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
        phone = this.phones.getOrNull(0),
        address = this.address,
        indebtedness = this.supplierIndebtedness,
        isClient = isClient,
        responsibleEmployeeLocalId = this.responsibleEmployee?.id,
        isSynced = false
    )
}