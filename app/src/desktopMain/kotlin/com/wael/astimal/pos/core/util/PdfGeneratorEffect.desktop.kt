package com.wael.astimal.pos.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.itextpdf.html2pdf.HtmlConverter
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun PdfGeneratorEffect(
    htmlContent: String?,
    baseFileName: String,
    onFinish: (String) -> Unit
) {
    if (htmlContent != null) {
        LaunchedEffect(htmlContent, baseFileName) {
            val downloadsDir = System.getProperty("user.home") + "/Downloads"
            val fileName = "${baseFileName.replace(" ", "_")}.pdf"
            val file = File(downloadsDir, fileName)

            try {
                if (file.exists()) {
                    file.delete()
                }
                val fileOutputStream = FileOutputStream(file)
                HtmlConverter.convertToPdf(htmlContent, fileOutputStream)
                println("✅ PDF successfully generated at: ${file.absolutePath}")
            } catch (e: Exception) {
                println("❌ Error generating PDF: ${e.message}")
                e.printStackTrace()
            } finally {
                onFinish("✅ PDF successfully generated at: ${file.absolutePath}")
            }
        }
    }
}