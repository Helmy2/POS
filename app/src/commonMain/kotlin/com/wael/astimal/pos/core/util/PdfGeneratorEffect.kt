package com.wael.astimal.pos.core.util

import androidx.compose.runtime.Composable

/**
 * A Composable side-effect to generate a PDF from an HTML string.
 * It observes the htmlContent and triggers generation when it's not null.
 *
 * @param htmlContent The HTML content to convert. If null, the effect does nothing.
 * @param baseFileName The base name for the output file.
 * @param onFinish A callback to signal that the generation process has been initiated.
 */
@Composable
expect fun PdfGeneratorEffect(
    htmlContent: String?,
    baseFileName: String,
    onFinish: (String) -> Unit
)