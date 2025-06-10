package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.domain.entity.Supplier
import kotlinx.coroutines.flow.Flow

interface SupplierRepository {
    fun getSuppliers(query: String=""): Flow<List<Supplier>>
}