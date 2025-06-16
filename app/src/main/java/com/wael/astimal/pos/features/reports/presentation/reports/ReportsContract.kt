package com.wael.astimal.pos.features.reports.presentation.reports

import androidx.annotation.StringRes
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.navigation.Destination


data class ReportsItem(val destination: Destination, @StringRes val label: Int)

data class ReportsState(
    val items: List<ReportsItem> = emptyList()
)

object ReportsDestinations {
    fun getAll(): List<ReportsItem> = listOf(
        ReportsItem(Destination.AccountStatement, R.string.account_statement),
    )
}