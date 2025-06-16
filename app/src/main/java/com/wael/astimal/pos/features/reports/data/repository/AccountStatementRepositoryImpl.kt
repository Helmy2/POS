package com.wael.astimal.pos.features.reports.data.repository

import com.wael.astimal.pos.core.util.toLocalDateTime
import com.wael.astimal.pos.features.management.data.local.OrderReturnDao
import com.wael.astimal.pos.features.management.data.local.PurchaseDao
import com.wael.astimal.pos.features.management.data.local.PurchaseReturnDao
import com.wael.astimal.pos.features.management.data.local.ReceivePayVoucherDao
import com.wael.astimal.pos.features.management.data.local.SalesOrderDao
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.reports.domain.entity.AccountTransaction
import com.wael.astimal.pos.features.reports.domain.entity.TransactionType
import com.wael.astimal.pos.features.reports.domain.repository.AccountStatementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime

class AccountStatementRepositoryImpl(
    private val salesOrderDao: SalesOrderDao,
    private val purchaseDao: PurchaseDao,
    private val salesReturnDao: OrderReturnDao,
    private val purchaseReturnDao: PurchaseReturnDao,
    private val receivePayVoucherDao: ReceivePayVoucherDao,
) : AccountStatementRepository {

    override fun getAccountStatement(partner: BusinessPartner): Flow<List<AccountTransaction>> {

        val salesFlow = partner.clientLocalId?.let {
            salesOrderDao.getSalesOrdersByClientId(it)
        } ?: flowOf(emptyList())

        val purchasesFlow = partner.supplierLocalId?.let {
            purchaseDao.getPurchasesBySupplierId(it)
        } ?: flowOf(emptyList())

        val salesReturnsFlow = partner.clientLocalId?.let {
            salesReturnDao.getReturnsByClientId(it)
        } ?: flowOf(emptyList())

        val purchaseReturnsFlow = partner.supplierLocalId?.let {
            purchaseReturnDao.getReturnsBySupplierId(it)
        } ?: flowOf(emptyList())

        val vouchersFlow = when {
            // Both Client and Supplier
            partner.clientLocalId != null && partner.supplierLocalId != null ->
                receivePayVoucherDao.getVouchersByPartnerIds(partner.clientLocalId, partner.supplierLocalId)
            // Only Client
            partner.clientLocalId != null ->
                receivePayVoucherDao.getVouchersByClientId(partner.clientLocalId)
            // Only Supplier
            partner.supplierLocalId != null ->
                receivePayVoucherDao.getVouchersBySupplierId(partner.supplierLocalId)
            else -> flowOf(emptyList())
        }

        // Combine all data flows into one.
        return combine(
            salesFlow,
            purchasesFlow,
            salesReturnsFlow,
            purchaseReturnsFlow,
            vouchersFlow
        ) { sales, purchases, salesReturns, purchaseReturns, vouchers ->
            val allTransactions = mutableListOf<AccountTransaction>()

            sales.mapTo(allTransactions) { order ->
                AccountTransaction(
                    date = order.order.orderDate.toLocalDateTime(),
                    transactionId = "SO-${order.order.localId}",
                    description = "Sales Invoice #${order.order.localId}",
                    transactionType = TransactionType.SALE,
                    debit = order.order.totalAmount,
                    credit = 0.0
                )
            }

            purchases.mapTo(allTransactions) { purchase ->
                AccountTransaction(
                    date = purchase.purchase.purchaseDate.toLocalDateTime(),
                    transactionId = "PO-${purchase.purchase.localId}",
                    description = "Purchase Invoice #${purchase.purchase.localId}",
                    transactionType = TransactionType.PURCHASE,
                    debit = 0.0,
                    credit = purchase.purchase.totalAmount
                )
            }

            salesReturns.mapTo(allTransactions) { aReturn ->
                AccountTransaction(
                    date = aReturn.orderReturn.returnDate.toLocalDateTime(),
                    transactionId = "SR-${aReturn.orderReturn.localId}",
                    description = "Sales Return #${aReturn.orderReturn.localId}",
                    transactionType = TransactionType.SALE_RETURN,
                    debit = 0.0,
                    credit = aReturn.orderReturn.totalAmount
                )
            }

            purchaseReturns.mapTo(allTransactions) { aReturn ->
                AccountTransaction(
                    date = aReturn.purchaseReturn.returnDate.toLocalDateTime(),
                    transactionId = "PR-${aReturn.purchaseReturn.localId}",
                    description = "Purchase Return #${aReturn.purchaseReturn.localId}",
                    transactionType = TransactionType.PURCHASE_RETURN,
                    debit = aReturn.purchaseReturn.totalAmount,
                    credit = 0.0
                )
            }

            vouchers.mapTo(allTransactions) { voucher ->
                if (voucher.voucher.isReceipt) { // Payment Received from Client
                    AccountTransaction(
                        date = voucher.voucher.date.toLocalDateTime(),
                        transactionId = "RV-${voucher.voucher.localId}",
                        description = "Payment Received #${voucher.voucher.localId}",
                        transactionType = TransactionType.PAYMENT_RECEIVED,
                        debit = 0.0,
                        credit = voucher.voucher.amount
                    )
                } else { // Payment Sent to Supplier
                    AccountTransaction(
                        date = voucher.voucher.date.toLocalDateTime(),
                        transactionId = "PV-${voucher.voucher.localId}",
                        description = "Payment Sent #${voucher.voucher.localId}",
                        transactionType = TransactionType.PAYMENT_SENT,
                        debit = voucher.voucher.amount,
                        credit = 0.0
                    )
                }
            }

            val sortedTransactions = allTransactions.sortedBy { it.date }

            val statementWithRunningBalance = mutableListOf<AccountTransaction>()
            var currentBalance = 0.0 // Start at zero, opening balance will be the first entry

            if (sortedTransactions.isNotEmpty()) {
                val openingBalanceValue = calculateOpeningBalance(partner.netBalance, sortedTransactions)
                statementWithRunningBalance.add(
                    AccountTransaction(
                        date = sortedTransactions.first().date.minusSeconds(1),
                        transactionId = "",
                        description = "Opening Balance",
                        transactionType = TransactionType.OPENING_BALANCE,
                        balance = openingBalanceValue
                    )
                )
                currentBalance = openingBalanceValue
            } else {
                // If there are no transactions, the statement is just the opening/current balance
                statementWithRunningBalance.add(
                    AccountTransaction(
                        date = LocalDateTime.now(),
                        transactionId = "",
                        description = "Current Balance",
                        transactionType = TransactionType.OPENING_BALANCE,
                        balance = partner.netBalance
                    )
                )
            }


            for (transaction in sortedTransactions) {
                currentBalance += transaction.debit - transaction.credit
                statementWithRunningBalance.add(transaction.copy(balance = currentBalance))
            }

            statementWithRunningBalance
        }
    }

    private fun calculateOpeningBalance(netBalance: Double, transactions: List<AccountTransaction>): Double {
        var openingBalance = netBalance
        transactions.forEach {
            openingBalance -= it.debit
            openingBalance += it.credit
        }
        return openingBalance
    }
}
