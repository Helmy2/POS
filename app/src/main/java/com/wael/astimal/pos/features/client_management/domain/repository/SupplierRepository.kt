package com.wael.astimal.pos.features.client_management.domain.repository

import com.wael.astimal.pos.features.client_management.domain.entity.Supplier
import kotlinx.coroutines.flow.Flow

interface SupplierRepository {
    fun searchSupplier(query: String=""): Flow<List<Supplier>>
}