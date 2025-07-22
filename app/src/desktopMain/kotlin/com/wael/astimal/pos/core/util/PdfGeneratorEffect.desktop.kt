package com.wael.astimal.pos.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.awt.Desktop
import java.io.File

@Composable
actual fun PdfGeneratorEffect(
    htmlContent: String?,
    baseFileName: String,
    onFinish: (String) -> Unit
) {
    if (htmlContent != null) {
        LaunchedEffect(htmlContent, baseFileName) {
            try {
                val tempFile = File.createTempFile(baseFileName.replace(" ", "_"), ".html")
                tempFile.writeText(htmlContent)
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(tempFile.toURI())
                    println("✅ HTML report successfully opened in browser: ${tempFile.absolutePath}")
                } else {
                    println("❌ Desktop operations not supported. Cannot open browser.")
                }
            } catch (e: Exception) {
                println("❌ Error generating PDF: ${e.message}")
                e.printStackTrace()
            } finally {
                onFinish("✅ HTML report successfully opened in browser")
            }
        }
    }
}