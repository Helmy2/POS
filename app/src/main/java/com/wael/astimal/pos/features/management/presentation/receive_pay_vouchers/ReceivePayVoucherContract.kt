package com.wael.astimal.pos.features.management.presentation.receive_pay_vouchers

import androidx.annotation.StringRes
import com.wael.astimal.pos.features.management.domain.entity.Client
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.entity.Supplier
import com.wael.astimal.pos.features.management.domain.entity.VoucherPartyType

data class ReceivePayVoucherState(
    val isLoading: Boolean = false,
    val vouchers: List<ReceivePayVoucher> = emptyList(),
    val clients: List<Client> = emptyList(),
    val suppliers: List<Supplier> = emptyList(),

    val partyType: VoucherPartyType = VoucherPartyType.CLIENT,
    val selectedClient: Client? = null,
    val selectedSupplier: Supplier? = null,
    val amount: String = "",
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),


    @StringRes val error: Int? = null,
    @StringRes val snackbarMessage: Int? = null
)

sealed interface ReceivePayVoucherEvent {
    data class SelectPartyType(val type: VoucherPartyType) : ReceivePayVoucherEvent
    data class SelectClient(val client: Client?) : ReceivePayVoucherEvent
    data class SelectSupplier(val supplier: Supplier?) : ReceivePayVoucherEvent
    data class UpdateAmount(val amount: String) : ReceivePayVoucherEvent
    data class UpdateNotes(val notes: String) : ReceivePayVoucherEvent
    data class UpdateDate(val date: Long) : ReceivePayVoucherEvent
    data object SaveVoucher : ReceivePayVoucherEvent
    data object ClearSnackbar : ReceivePayVoucherEvent
    data object ClearError : ReceivePayVoucherEvent
}
