package com.wael.astimal.pos.features.management.presentaion.supplier_info

import androidx.annotation.StringRes
import com.wael.astimal.pos.features.management.domain.entity.Supplier

data class SupplierInfoState(
    val loading: Boolean = false,
    val searchResults: List<Supplier> = emptyList(),
    val selectedSupplier: Supplier? = null,
    val query: String = "",
    @StringRes val error: Int? = null,
    val snackbarMessage: String? = null,
    val showDetailDialog: Boolean = false,
)

sealed interface SupplierInfoEvent {
    data class SearchSuppliers(val query: String) : SupplierInfoEvent
    data class SelectSupplier(val supplier: Supplier?) : SupplierInfoEvent
    data object ClearSnackbar : SupplierInfoEvent
    data class UpdateQuery(val query: String) : SupplierInfoEvent
    data object DetailSupplier : SupplierInfoEvent
    data object ShowDetailDialog : SupplierInfoEvent
}