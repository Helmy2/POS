package com.wael.astimal.pos.features.management.presentation.receive_pay_vouchers

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.entity.VoucherPartyType
import com.wael.astimal.pos.features.user.domain.entity.User

data class ReceivePayVoucherState(
    val isLoading: Boolean = false,
    val vouchers: List<ReceivePayVoucher> = emptyList(),
    val clients: List<BusinessPartner> = emptyList(),
    val suppliers: List<BusinessPartner> = emptyList(),
    val partyType: VoucherPartyType = VoucherPartyType.CLIENT,
    val selectedClient: BusinessPartner? = null,
    val selectedSupplier: BusinessPartner? = null,
    val amount: String = "",
    val notes: String = "",
    val date: Long = Clock.now(),
    val isSaving: Boolean = false,
    val currentUser: User? = null,
    val voucherToEdit: ReceivePayVoucher? = null,
    val showEditDialog: Boolean = false
) {
    val canEdit = currentUser?.isAdmin == true ||
            (selectedClient?.responsibleEmployee?.id == currentUser?.id && partyType == VoucherPartyType.CLIENT)
            || (selectedSupplier?.responsibleEmployee?.id == currentUser?.id && partyType == VoucherPartyType.SUPPLIER)
}

sealed interface ReceivePayVoucherEvent {
    data class SelectPartyType(val type: VoucherPartyType) : ReceivePayVoucherEvent
    data class SelectClient(val client: BusinessPartner?) : ReceivePayVoucherEvent
    data class SelectSupplier(val supplier: BusinessPartner?) : ReceivePayVoucherEvent
    data class UpdateAmount(val amount: String) : ReceivePayVoucherEvent
    data class UpdateNotes(val notes: String) : ReceivePayVoucherEvent
    data class UpdateDate(val date: Long) : ReceivePayVoucherEvent
    data object AddVoucher : ReceivePayVoucherEvent
    data class EditVoucherClicked(val voucher: ReceivePayVoucher) : ReceivePayVoucherEvent
    data class DeleteVoucherClicked(val voucher: ReceivePayVoucher) : ReceivePayVoucherEvent
    data class SaveVoucher(val voucher: ReceivePayVoucher) : ReceivePayVoucherEvent
    data object DismissEditDialog : ReceivePayVoucherEvent
}
