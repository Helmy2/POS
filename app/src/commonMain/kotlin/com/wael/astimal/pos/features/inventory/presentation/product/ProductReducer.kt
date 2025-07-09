package com.wael.astimal.pos.features.inventory.presentation.product

import com.wael.astimal.pos.core.base.mvi.Reducer

class ProductReducer : Reducer<ProductContract.State, ProductContract.Event, Nothing> {
    override fun reduce(
        previousState: ProductContract.State,
        event: ProductContract.Event
    ): Pair<ProductContract.State, Nothing?> {
        return when (event) {
            is ProductContract.Event.LoadingStarted ->
                previousState.copy(isLoading = true) to null

            is ProductContract.Event.LoadingFinished ->
                previousState.copy(isLoading = false) to null

            is ProductContract.Event.SearchQueryChanged ->
                previousState.copy(searchQuery = event.query) to null

            is ProductContract.Event.SearchActiveChanged ->
                previousState.copy(isSearchActive = event.isActive) to null

            is ProductContract.Event.UserLoaded ->
                previousState.copy(currentUser = event.user) to null

            is ProductContract.Event.DropdownDataLoaded ->
                previousState.copy(dropdownData = event.data) to null

            is ProductContract.Event.ProductsLoaded ->
                previousState.copy(isLoading = false, products = event.products) to null

            is ProductContract.Event.ProductSelected ->
                previousState.copy(
                    selectedProduct = event.product,
                    inputArName = event.product.name.arName ?: "",
                    inputEnName = event.product.name.enName ?: "",
                    inputPurchasePrice = event.product.averagePrice.toString(),
                    inputSellingPrice = event.product.sellingPrice.toString(),

                    inputSubUnitsPerMainUnit = event.product.subUnitsPerMainUnit.toString(),
                    selectedCategoryId = event.product.category?.id?.local,
                    // remove the selected store id
                    selectedSubUnitId = event.product.subProductUnit?.id?.local,
                    selectedMainUnitId = event.product.mainProductUnit.id.local,
                    isSearchActive = false
                ) to null

            is ProductContract.Event.NewProductClicked,
            is ProductContract.Event.SaveSucceeded,
            is ProductContract.Event.DeleteSucceeded ->
                // Clear the form
                previousState.copy(
                    isLoading = false,
                    selectedProduct = null,
                    inputArName = "",
                    inputEnName = "",
                    inputPurchasePrice = "",
                    inputSellingPrice = "",
                    inputSubUnitsPerMainUnit = "1",
                    selectedCategoryId = null,
                    selectedMainUnitId = null,
                    selectedSubUnitId = null
                ) to null

            // Form input updates
            is ProductContract.Event.ArNameChanged -> previousState.copy(inputArName = event.name) to null
            is ProductContract.Event.EnNameChanged -> previousState.copy(inputEnName = event.name) to null
            is ProductContract.Event.PurchasePriceChanged -> previousState.copy(inputPurchasePrice = event.price) to null
            is ProductContract.Event.SellingPriceChanged -> previousState.copy(inputSellingPrice = event.price) to null
            is ProductContract.Event.SubUnitsPerMainUnitChanged -> previousState.copy(
                inputSubUnitsPerMainUnit = event.value
            ) to null

            is ProductContract.Event.CategoryIdChanged -> previousState.copy(selectedCategoryId = event.id) to null
            is ProductContract.Event.SubUnitIdChanged -> previousState.copy(
                selectedSubUnitId = event.id
            ) to null

            is ProductContract.Event.MainUnitIdChanged -> previousState.copy(
                selectedMainUnitId = event.id
            ) to null

            is ProductContract.Event.DeleteClicked -> previousState.copy(
                showDeleteDialog = true
            ) to null

            is ProductContract.Event.DeleteCanceled, is ProductContract.Event.DeleteConfirmed -> previousState.copy(
                showDeleteDialog = false
            ) to null

            is ProductContract.Event.BackClicked,
            is ProductContract.Event.SaveClicked
                -> previousState to null
        }
    }
}
