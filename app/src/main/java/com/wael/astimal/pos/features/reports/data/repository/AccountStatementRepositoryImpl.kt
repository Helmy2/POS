package com.wael.astimal.pos.features.reports.data.repository

import com.wael.astimal.pos.core.util.toLocalDateTime
import com.wael.astimal.pos.features.management.data.local.ClientDao
import com.wael.astimal.pos.features.management.data.local.OrderReturnDao
import com.wael.astimal.pos.features.management.data.local.PurchaseDao
import com.wael.astimal.pos.features.management.data.local.PurchaseReturnDao
import com.wael.astimal.pos.features.management.data.local.ReceivePayVoucherDao
import com.wael.astimal.pos.features.management.data.local.SalesOrderDao
import com.wael.astimal.pos.features.management.data.local.SupplierDao
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
    private val clientDao: ClientDao,
    private val supplierDao: SupplierDao
) : AccountStatementRepository {

    override fun getAccountStatement(partner: BusinessPartner): Flow<List<AccountTransaction>> {
        val clientDebtFlow = partner.clientLocalId?.let { clientDao.getDebtFlow(it) } ?: flowOf(0.0)
        val supplierIndebtednessFlow =
            partner.supplierLocalId?.let { supplierDao.getIndebtednessFlow(it) } ?: flowOf(0.0)

        val transactionsFlow = getTransactionsFlow(partner)

        return combine(
            clientDebtFlow,
            supplierIndebtednessFlow,
            transactionsFlow
        ) { clientDebt, supplierIndebtedness, transactions ->

            val currentNetBalance = (clientDebt ?: 0.0) - (supplierIndebtedness ?: 0.0)

            var openingBalance = currentNetBalance
            transactions.forEach { transaction ->
                openingBalance -= (transaction.debit - transaction.credit)
            }

            val statement = mutableListOf<AccountTransaction>()
            statement.add(
                AccountTransaction(
                    date = if (transactions.isNotEmpty()) transactions.first().date.minusNanos(1) else LocalDateTime.now(),
                    invoiceNumber = "",
                    transactionId = "OPENING",
                    transactionType = TransactionType.OPENING_BALANCE,
                    balance = openingBalance
                )
            )

            var runningBalance = openingBalance
            for (transaction in transactions) {
                runningBalance += (transaction.debit - transaction.credit)
                statement.add(transaction.copy(balance = runningBalance))
            }
            statement
        }
    }

    private fun getTransactionsFlow(partner: BusinessPartner): Flow<List<AccountTransaction>> {
        val salesFlow =
            partner.clientLocalId?.let { salesOrderDao.getSalesOrdersByClientId(it) } ?: flowOf(
                emptyList()
            )
        val purchasesFlow =
            partner.supplierLocalId?.let { purchaseDao.getPurchasesBySupplierId(it) } ?: flowOf(
                emptyList()
            )
        val salesReturnsFlow =
            partner.clientLocalId?.let { salesReturnDao.getReturnsByClientId(it) } ?: flowOf(
                emptyList()
            )
        val purchaseReturnsFlow =
            partner.supplierLocalId?.let { purchaseReturnDao.getReturnsBySupplierId(it) } ?: flowOf(
                emptyList()
            )
        val vouchersFlow = when {
            partner.clientLocalId != null && partner.supplierLocalId != null ->
                receivePayVoucherDao.getVouchersByPartnerIds(partner.clientLocalId, partner.supplierLocalId)
            partner.clientLocalId != null -> receivePayVoucherDao.getVouchersByClientId(partner.clientLocalId)
            partner.supplierLocalId != null -> receivePayVoucherDao.getVouchersBySupplierId(partner.supplierLocalId)
            else -> flowOf(emptyList())
        }

        return combine(
            salesFlow,
            purchasesFlow,
            salesReturnsFlow,
            purchaseReturnsFlow,
            vouchersFlow
        ) { sales, purchases, salesReturns, purchaseReturns, vouchers ->
            val allTransactions = mutableListOf<AccountTransaction>()

            // --- CORRECTED MAPPING LOGIC for Debit and Credit ---
            sales.mapTo(allTransactions) { order ->
                AccountTransaction(
                    date = order.order.orderDate.toLocalDateTime(),
                    transactionId = "SO-${order.order.localId}",
                    invoiceNumber = order.order.invoiceNumber ?: "N/A",
                    transactionType = TransactionType.SALE,
                    debit = order.order.totalAmount // A sale increases the amount the client owes you.
                )
            }
            purchases.mapTo(allTransactions) { purchase ->
                AccountTransaction(
                    date = purchase.purchase.purchaseDate.toLocalDateTime(),
                    transactionId = "PO-${purchase.purchase.localId}",
                    invoiceNumber = purchase.purchase.invoiceNumber ?: "N/A",
                    transactionType = TransactionType.PURCHASE,
                    credit = purchase.purchase.totalAmount // A purchase increases the amount you owe the supplier.
                )
            }
            salesReturns.mapTo(allTransactions) { aReturn ->
                AccountTransaction(
                    date = aReturn.orderReturn.returnDate.toLocalDateTime(),
                    transactionId = "SR-${aReturn.orderReturn.localId}",
                    invoiceNumber = aReturn.orderReturn.invoiceNumber ?: "N/A",
                    transactionType = TransactionType.SALE_RETURN,
                    credit = aReturn.orderReturn.totalAmount // A sales return from a client is a credit to them.
                )
            }
            purchaseReturns.mapTo(allTransactions) { aReturn ->
                AccountTransaction(
                    date = aReturn.purchaseReturn.returnDate.toLocalDateTime(),
                    transactionId = "PR-${aReturn.purchaseReturn.localId}",
                    invoiceNumber = aReturn.purchaseReturn.invoiceNumber ?: "N/A",
                    transactionType = TransactionType.PURCHASE_RETURN,
                    debit = aReturn.purchaseReturn.totalAmount // A purchase return to a supplier is a debit to them.
                )
            }
            vouchers.mapTo(allTransactions) { voucher ->
                if (voucher.voucher.isReceipt) { // Payment Received from Client
                    AccountTransaction(
                        date = voucher.voucher.date.toLocalDateTime(),
                        transactionId = "RV-${voucher.voucher.localId}",
                        invoiceNumber = "Voucher #${voucher.voucher.localId}",
                        transactionType = TransactionType.PAYMENT_RECEIVED,
                        credit = voucher.voucher.amount // Receiving money from a client is a credit.
                    )
                } else { // Payment Sent to Supplier
                    AccountTransaction(
                        date = voucher.voucher.date.toLocalDateTime(),
                        transactionId = "PV-${voucher.voucher.localId}",
                        invoiceNumber = "Voucher #${voucher.voucher.localId}",
                        transactionType = TransactionType.PAYMENT_SENT,
                        debit = voucher.voucher.amount // Sending money to a supplier is a debit.
                    )
                }
            }

            allTransactions.sortedBy { it.date }
        }
    }
}
