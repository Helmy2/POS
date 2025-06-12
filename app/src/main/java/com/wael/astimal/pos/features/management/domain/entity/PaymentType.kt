package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.R

enum class PaymentType {
    CASH, TRANSFER, WALLET, DEFERRED;

    fun stringResource(type: PaymentType = this): Int {
        return when (type) {
            CASH -> R.string.payment_type_cash
            TRANSFER -> R.string.payment_type_transfer
            WALLET -> R.string.payment_type_wallet
            DEFERRED -> R.string.payment_type_deferred
        }
    }
}