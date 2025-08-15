package com.wael.astimal.pos.features.reports.presentation.employee_report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.core.domain.entity.get
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.DataPicker
import com.wael.astimal.pos.core.util.PdfGeneratorEffect
import com.wael.astimal.pos.features.reports.domain.model.EmployeeActivity
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pos.app.generated.resources.Res
import pos.app.generated.resources.apply_filters
import pos.app.generated.resources.date
import pos.app.generated.resources.description
import pos.app.generated.resources.employee_activity_report
import pos.app.generated.resources.end_date
import pos.app.generated.resources.invoice_details_format
import pos.app.generated.resources.partner_payment
import pos.app.generated.resources.partner_payment_details_format
import pos.app.generated.resources.select_employee
import pos.app.generated.resources.start_date
import pos.app.generated.resources.total_amount
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeReportRoute(
    onNavigateBack: () -> Unit,
    viewModel: EmployeeReportViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    PdfGeneratorEffect(
        htmlContent = state.pdfHtmlToGenerate,
        baseFileName = "employee_activity_report_${state.selectedEmployee?.name ?: "report"}",
        onFinish = { viewModel.processEvent(EmployeeReportContract.Event.PdfGenerationFinished(it)) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.employee_activity_report)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.processEvent(EmployeeReportContract.Event.GeneratePdf) },
                        enabled = state.activities.isNotEmpty()
                    ) { Icon(Icons.Default.PictureAsPdf, "Generate PDF") }
                }
            )
        }
    ) { paddingValues ->
        EmployeeReportScreen(
            state = state,
            processEvent = viewModel::processEvent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun EmployeeReportScreen(
    state: EmployeeReportContract.State,
    processEvent: (EmployeeReportContract.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        DataPicker(
            onDateSelected = { processEvent(EmployeeReportContract.Event.SetStartDate(it)) },
            onDismiss = { showStartDatePicker = false }
        )
    }
    if (showEndDatePicker) {
        DataPicker(
            onDateSelected = { processEvent(EmployeeReportContract.Event.SetEndDate(it)) },
            onDismiss = { showEndDatePicker = false }
        )
    }

    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {

        CustomExposedDropdownMenu(
            label = stringResource(Res.string.select_employee),
            currentSelection = state.selectedEmployee?.name ?: "",
            items = state.employees,
            onItemSelected = { processEvent(EmployeeReportContract.Event.SelectEmployee(it.id)) },
            itemToDisplayString = { it.name },
            modifier = Modifier.padding(8.dp)
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.startDate.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.start_date)) },
            trailingIcon = {
                IconButton(onClick = {
                    showStartDatePicker = true
                }) { Icon(Icons.Default.DateRange, null) }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.endDate.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.end_date)) },
            trailingIcon = {
                IconButton(onClick = {
                    showEndDatePicker = true
                }) { Icon(Icons.Default.DateRange, null) }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { processEvent(EmployeeReportContract.Event.ApplyFilters) },
            enabled = state.selectedEmployeeId != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(Res.string.apply_filters)) }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()

        // --- Report Data ---
        if (state.isLoading) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.activities.isNotEmpty()) {
                    item {
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp)) {
                            Text(
                                stringResource(Res.string.date),
                                modifier = Modifier.weight(2f),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                stringResource(Res.string.description),
                                modifier = Modifier.weight(3f),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                stringResource(Res.string.total_amount),
                                modifier = Modifier.weight(1.5f),
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
                items(state.activities) { activity ->
                    ActivityRow(activity) {
                        processEvent(EmployeeReportContract.Event.SelectActivity(activity))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun ActivityRow(activity: EmployeeActivity, onClick: () -> Unit) {
    val date = activity.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date

    // --- NEW LOCALIZATION LOGIC ---
    // All UI text is now derived here, inside the Composable, where it can access resources.
    val typeString: String
    val detailsString: String
    val amount: Double

    when (activity) {
        is EmployeeActivity.InvoiceActivity -> {
            typeString = stringResource(activity.invoice.invoiceType.getStringResId())
            detailsString = stringResource(
                Res.string.invoice_details_format,
                activity.invoice.id,
                activity.invoice.partner.name.get()
            )
            amount = activity.invoice.totalAmount
        }

        is EmployeeActivity.FinancialActivity -> {
            typeString = stringResource(activity.transaction.type.getStringResId())
            detailsString = activity.transaction.notes ?: typeString
            amount = activity.transaction.amount
        }

        is EmployeeActivity.PartnerPaymentActivity -> {
            typeString = stringResource(Res.string.partner_payment)
            detailsString = stringResource(
                Res.string.partner_payment_details_format,
                activity.transaction.partner.name.get()
            )
            amount = activity.transaction.amount
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                date.toString(),
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.bodyMedium
            )
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    typeString,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                if (detailsString != typeString) { // Only show details if they are different from the type
                    Text(
                        detailsString,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                String.format("%.2f", amount),
                modifier = Modifier.weight(1.5f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End
            )
        }
    }
}