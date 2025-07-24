package com.wael.astimal.pos.features.reports.presentation.product_movement

import com.wael.astimal.pos.core.base.mvi.Reducer

class ProductMovementReducer :
    Reducer<ProductMovementContract.State, ProductMovementContract.Event, ProductMovementContract.Effect> {
    override fun reduce(
        previousState: ProductMovementContract.State,
        event: ProductMovementContract.Event,
    ): Pair<ProductMovementContract.State, ProductMovementContract.Effect?> {
        return when (event) {
            is ProductMovementContract.Event.ShowInitialData -> previousState.copy(
                products = event.products,
                stores = event.stores
            ) to null

            is ProductMovementContract.Event.SelectProduct -> previousState.copy(selectedProductId = event.productId) to null
            is ProductMovementContract.Event.SelectStore -> previousState.copy(selectedStoreId = event.storeId) to null
            is ProductMovementContract.Event.SetStartDate -> previousState.copy(startDate = event.date) to null
            is ProductMovementContract.Event.SetEndDate -> previousState.copy(endDate = event.date) to null
            is ProductMovementContract.Event.ApplyFilters -> previousState.copy(
                isLoading = true,
                movementGroups = emptyList()
            ) to null

            is ProductMovementContract.Event.ShowMovement -> previousState.copy(
                isLoading = false,
                movementGroups = event.groups
            ) to null

            is ProductMovementContract.Event.PdfGenerationFinished -> previousState.copy(
                pdfHtmlToGenerate = null
            ) to null

            is ProductMovementContract.Event.PdfGenerationSuccess -> previousState.copy(
                pdfHtmlToGenerate = event.pdfHtml
            ) to null

            else -> previousState to null
        }
    }
}