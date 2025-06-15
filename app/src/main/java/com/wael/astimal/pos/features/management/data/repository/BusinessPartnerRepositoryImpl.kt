package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.management.domain.repository.SupplierRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class BusinessPartnerRepositoryImpl(
    private val clientRepository: ClientRepository,
    private val supplierRepository: SupplierRepository
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
                    clientDebt = client.debt ?: 0.0
                )
                partnerMap[client.name.enName.orEmpty().lowercase()] = partner
            }

            suppliers.forEach { supplier ->
                val existingPartner = partnerMap[supplier.name.enName.orEmpty().lowercase()]
                if (existingPartner != null) {
                    // This entity is both a client and a supplier
                    partnerMap[supplier.name.enName.orEmpty().lowercase()] = existingPartner.copy(
                        type = PartnerType.BOTH,
                        supplierLocalId = supplier.id,
                        supplierIndebtedness = supplier.indebtedness ?: 0.0
                    )
                } else {
                    // This entity is only a supplier
                    val partner = BusinessPartner(
                        clientLocalId = null,
                        supplierLocalId = supplier.id,
                        name = LocalizedString(
                            enName = supplier.name.enName,
                            arName = supplier.name.arName
                        ),
                        address = supplier.address,
                        phones = supplier.phones,
                        responsibleEmployee = supplier.responsibleEmployee,
                        type = PartnerType.SUPPLIER,
                        supplierIndebtedness = supplier.indebtedness ?: 0.0
                    )
                    partnerMap[supplier.name.enName.orEmpty().lowercase()] = partner
                }
            }
            partnerMap.values.toList()
        }
    }

    override suspend fun saveBusinessPartner(partner: BusinessPartner): Result<Unit> {
        // This logic will be more complex and will involve creating/updating
        // records in both the client and supplier repositories based on the partner type.
        // For now, we will leave it as not implemented.
        return Result.failure(UnsupportedOperationException("Save is not yet implemented for Business Partners."))
    }

    override suspend fun deleteBusinessPartner(partner: BusinessPartner): Result<Unit> {
        // Similar to save, delete logic needs to handle multiple tables.
        return Result.failure(UnsupportedOperationException("Delete is not yet implemented for Business Partners."))
    }
}
