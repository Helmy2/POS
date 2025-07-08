package com.wael.astimal.pos.features.management.presentation.receive_pay_vouchers

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock

class ReceivePayVoucherReducer() :
    Reducer<ReceivePayVoucherContract.State, ReceivePayVoucherContract.Event, Nothing> {
    override fun reduce(
        previousState: ReceivePayVoucherContract.State,
        event: ReceivePayVoucherContract.Event
    ): Pair<ReceivePayVoucherContract.State, Nothing?> {
        return when (event) {
            is ReceivePayVoucherContract.Event.LoadInitialData ->
                previousState.copy(isLoading = true) to null

            is ReceivePayVoucherContract.Event.UserLoaded ->
                previousState.copy(currentUser = event.user) to null

            is ReceivePayVoucherContract.Event.DropdownDataLoaded ->
                previousState.copy(partyDropdownData = event.data) to null

            is ReceivePayVoucherContract.Event.VouchersLoaded ->
                previousState.copy(isLoading = false, vouchers = event.vouchers) to null

            is ReceivePayVoucherContract.Event.AddVoucherClicked ->
                previousState.copy(dialogState = previousState.dialogState.copy(show = true)) to null

            is ReceivePayVoucherContract.Event.EditVoucherClicked ->
                previousState.copy(
                    dialogState = ReceivePayVoucherContract.DialogState(
                        show = true,
                        voucherToEdit = event.voucher,
                        selectedPartnerId = event.voucher.partner.id.local,
                        amount = event.voucher.amount.toString(),
                        notes = event.voucher.notes,
                        date = event.voucher.createdAt
                    )
                ) to null

            is ReceivePayVoucherContract.Event.DismissDialog,
            is ReceivePayVoucherContract.Event.SaveSucceeded ->
                previousState.copy(
                    dialogState = ReceivePayVoucherContract.DialogState(
                        show = false,
                        date = Clock.now()
                    )
                ) to null

            // Dialog state updates

            is ReceivePayVoucherContract.Event.DialogPartnerSelected ->
                previousState.copy(dialogState = previousState.dialogState.copy(selectedPartnerId = event.partnerId)) to null

            is ReceivePayVoucherContract.Event.DialogAmountChanged ->
                previousState.copy(dialogState = previousState.dialogState.copy(amount = event.amount)) to null

            is ReceivePayVoucherContract.Event.DialogNotesChanged ->
                previousState.copy(dialogState = previousState.dialogState.copy(notes = event.notes)) to null

            is ReceivePayVoucherContract.Event.DialogDateChanged ->
                previousState.copy(dialogState = previousState.dialogState.copy(date = event.date)) to null

            is ReceivePayVoucherContract.Event.DialogTransactionTypeSelected ->
                previousState.copy(dialogState = previousState.dialogState.copy(transactionType = event.type)) to null

            is ReceivePayVoucherContract.Event.SearchQueryChanged ->
                previousState.copy(searchQuery = event.query) to null

            is ReceivePayVoucherContract.Event.BackClicked,
            is ReceivePayVoucherContract.Event.DeleteVoucherClicked,
            is ReceivePayVoucherContract.Event.SaveChangesClicked -> previousState to null
        }
    }
}
