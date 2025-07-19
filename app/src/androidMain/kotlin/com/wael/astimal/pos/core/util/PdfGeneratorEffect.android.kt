package com.wael.astimal.pos.core.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun PdfGeneratorEffect(
    htmlContent: String?,
    baseFileName: String,
    onFinish: (String) -> Unit
) {
    val context = LocalActivity.current

    if (htmlContent != null) {
        LaunchedEffect(htmlContent, baseFileName) {
            val activity = context ?: run {
                onFinish("Error: PDF generation requires an Activity context.")
                return@LaunchedEffect
            }

            val webView = WebView(activity)

            webView.layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)

            val printManager = activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "${activity.packageName} Document"
            val printAdapter = webView.createPrintDocumentAdapter(jobName)

            printManager.print(
                jobName,
                printAdapter,
                PrintAttributes.Builder().build()
            )

            onFinish("PDF generated successfully.")
        }
    }
}