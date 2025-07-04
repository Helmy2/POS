package com.wael.astimal.pos.features.management.domain.entity

import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.payment_type_cash
import pos.app.generated.resources.payment_type_deferred
import pos.app.generated.resources.payment_type_transfer
import pos.app.generated.resources.payment_type_undefined
import pos.app.generated.resources.payment_type_wallet

enum class PaymentType {
    CASH, TRANSFER, WALLET, DEFERRED, UNDEFINED;

    fun stringResource(type: PaymentType = this): StringResource {
        return when (type) {
            CASH -> Res.string.payment_type_cash
            TRANSFER -> Res.string.payment_type_transfer
            WALLET -> Res.string.payment_type_wallet
            DEFERRED -> Res.string.payment_type_deferred
            UNDEFINED -> Res.string.payment_type_undefined
        }
    }

    companion object {
        fun getFormServerValue(value: String): PaymentType {
            return when (value) {
                "case" -> CASH
                "transfer" -> TRANSFER
                "wallet" -> WALLET
                "deferred" -> DEFERRED
                else -> UNDEFINED
            }
        }
    }
}