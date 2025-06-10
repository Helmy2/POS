package com.wael.astimal.pos.features.client_management.data.repository

import com.wael.astimal.pos.features.client_management.data.entity.toDomain
import com.wael.astimal.pos.features.client_management.data.local.SupplierDao
import com.wael.astimal.pos.features.client_management.domain.entity.Supplier
import com.wael.astimal.pos.features.client_management.domain.repository.SupplierRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SupplierRepositoryImpl(private val supplierDao: SupplierDao) :
    SupplierRepository {
    override fun getSuppliers(query: String): Flow<List<Supplier>> {
        return supplierDao.searchSuppliersWithDetailsFlow(query).map { list ->
            list.map { it.toDomain() }
        }
    }
}

