package com.wael.astimal.pos.features.management.presentaion.purchase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import org.koin.androidx.compose.koinViewModel

@Composable
fun PurchaseRoute(
    viewModel: PurchaseViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PurchaseScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        onBack = onBack
    )
}

@Composable
fun PurchaseScreen(
    state: PurchaseScreenState,
    onEvent: (PurchaseScreenEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(state.snackbarMessage, state.error) {
        state.snackbarMessage?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(PurchaseScreenEvent.ClearSnackbar)
        }
        state.error?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            onEvent(PurchaseScreenEvent.ClearSnackbar)
        }
    }

    SearchScreen(
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        onQueryChange = { onEvent(PurchaseScreenEvent.UpdateQuery(it)) },
        onSearch = { onEvent(PurchaseScreenEvent.SearchPurchases(it)) },
        onSearchActiveChange = { onEvent(PurchaseScreenEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        onDelete = { onEvent(PurchaseScreenEvent.DeletePurchase) },
        onCreate = { onEvent(PurchaseScreenEvent.SavePurchase) },
        onUpdate = { onEvent(PurchaseScreenEvent.SavePurchase) }, // Update logic can be added later
        onNew = { onEvent(PurchaseScreenEvent.OpenNewPurchaseForm) },
        searchResults = {
            ItemGrid(
                list = state.purchases,
                onItemClick = { onEvent(PurchaseScreenEvent.SelectPurchaseToView(it)) },
                label = {
                    Text(
                        "Purchase from ${it.supplier?.name?.displayName(LocalAppLocale.current)}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                },
                isSelected = { purchase -> purchase.localId == state.selectedPurchase?.localId },
            )
        },
        mainContent = {
            PurchaseForm(state = state, onEvent = onEvent)
        }
    )
}

@Composable
fun PurchaseForm(
    state: PurchaseScreenState,
    onEvent: (PurchaseScreenEvent) -> Unit
) {
    val currentLanguage = LocalAppLocale.current
    val purchaseInput = state.currentPurchaseInput
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CustomExposedDropdownMenu(
            label = stringResource(R.string.supplier),
            items = state.availableSuppliers,
            selectedItemId = purchaseInput.selectedSupplier?.id,
            onItemSelected = { onEvent(PurchaseScreenEvent.SelectSupplier(it)) },
            itemToDisplayString = { it.name.displayName(currentLanguage) },
            itemToId = { it.id }
        )

        CustomExposedDropdownMenu(
            label = "Employee",
            items = state.availableEmployees,
            selectedItemId = purchaseInput.selectedEmployeeId,
            onItemSelected = { onEvent(PurchaseScreenEvent.SelectEmployee(it?.id)) },
            itemToDisplayString = { it.localizedName.displayName(currentLanguage) },
            itemToId = { it.id },
        )

        Text("Items", style = MaterialTheme.typography.titleMedium)
        purchaseInput.items.forEach { item ->
            PurchaseItemRow(item = item, state = state, onEvent = onEvent)
        }
        Button(
            onClick = { onEvent(PurchaseScreenEvent.AddItemToPurchase) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.add_item))
        }

        CustomExposedDropdownMenu(
            label = stringResource(R.string.payment_type),
            items = PaymentType.entries,
            selectedItemId = purchaseInput.paymentType.ordinal.toLong(),
            onItemSelected = {
                onEvent(
                    PurchaseScreenEvent.UpdatePaymentType(
                        it ?: PaymentType.CASH
                    )
                )
            },
            itemToDisplayString = { it.name },
            itemToId = { it.ordinal.toLong() }
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Price:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "%.2f".format(purchaseInput.totalPrice),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PurchaseItemRow(
    item: EditablePurchaseItem,
    state: PurchaseScreenState,
    onEvent: (PurchaseScreenEvent) -> Unit
) {
    val language = LocalAppLocale.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.product),
                    items = state.availableProducts,
                    selectedItemId = item.product?.localId,
                    onItemSelected = { product ->
                        onEvent(
                            PurchaseScreenEvent.UpdateItemProduct(
                                item.tempEditorId,
                                product
                            )
                        )
                    },
                    itemToDisplayString = { it.localizedName.displayName(language) },
                    itemToId = { it.localId }
                )
            }
            IconButton(onClick = { onEvent(PurchaseScreenEvent.RemoveItemFromPurchase(item.tempEditorId)) }) {
                Icon(Icons.Default.Delete, "Remove Item")
            }
        }

        CustomExposedDropdownMenu(
            label = stringResource(R.string.unit),
            items = listOf(
                item.product?.minimumUnit, item.product?.maximumUnit
            ),
            selectedItemId = item.selectedUnit?.localId,
            onItemSelected = { unit ->
                onEvent(
                    PurchaseScreenEvent.UpdateItemUnit(
                        item.tempEditorId, unit
                    )
                )
            },
            itemToDisplayString = { it?.localizedName?.displayName(language) ?: "" },
            itemToId = { it?.localId ?: -1L },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = item.quantity,
                onValueChange = {
                    onEvent(
                        PurchaseScreenEvent.UpdateItemQuantity(
                            item.tempEditorId,
                            it
                        )
                    )
                },
                label = { Text(stringResource(R.string.qty)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = item.purchasePrice,
                onValueChange = {
                    onEvent(
                        PurchaseScreenEvent.UpdateItemPrice(
                            item.tempEditorId,
                            it
                        )
                    )
                },
                label = { Text(stringResource(R.string.price)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = "%.2f".format(item.lineTotal),
                onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.total)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}