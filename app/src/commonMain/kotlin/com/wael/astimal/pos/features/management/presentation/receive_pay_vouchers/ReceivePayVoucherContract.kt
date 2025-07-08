package com.wael.astimal.pos.features.management.presentation.receive_pay_vouchers

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.data.entity.TransactionType
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.user.domain.entity.User

object ReceivePayVoucherContract {
    data class DialogState(
        val show: Boolean = false,
        val voucherToEdit: ReceivePayVoucher? = null,
        val selectedPartnerId: Long? = null,
        val transactionType: TransactionType = TransactionType.PAYMENT,
        val amount: String = "",
        val notes: String = "",
        val date: Long = Clock.now(),
    )

    data class State(
        val isLoading: Boolean = true,
        val vouchers: List<ReceivePayVoucher> = emptyList(),
        val partyDropdownData: List<BusinessPartner> = emptyList(),
        val currentUser: User? = null,
        val dialogState: DialogState = DialogState(),
        val searchQuery: String = "",
    ) : Reducer.ViewState {
        val canUserEdit: Boolean get() = currentUser?.isAdmin == true
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data object LoadInitialData : Event
        data object AddVoucherClicked : Event
        data class SearchQueryChanged(val query: String) : Event
        data class EditVoucherClicked(val voucher: ReceivePayVoucher) : Event
        data class DeleteVoucherClicked(val voucher: ReceivePayVoucher) : Event
        data object SaveChangesClicked : Event
        data object DismissDialog : Event
        data object BackClicked : Event

        // Dialog Input Changes
        data class DialogPartnerSelected(val partnerId: Long?) : Event
        data class DialogAmountChanged(val amount: String) : Event
        data class DialogNotesChanged(val notes: String) : Event
        data class DialogDateChanged(val date: Long) : Event
        data class DialogTransactionTypeSelected(val type: TransactionType) : Event

        // Data results from ViewModel
        data class UserLoaded(val user: User?) : Event
        data class DropdownDataLoaded(val data: List<BusinessPartner>) : Event
        data class VouchersLoaded(val vouchers: List<ReceivePayVoucher>) : Event
        data object SaveSucceeded : Event
    }
}
