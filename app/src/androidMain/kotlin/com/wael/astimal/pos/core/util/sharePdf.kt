package com.wael.astimal.pos.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat

fun sharePdf(context: Context, uri: Uri, title: String) {
    val shareIntent = ShareCompat.IntentBuilder(context)
        .setType("application/pdf")
        .setStream(uri)
        .setChooserTitle(title)
        .createChooserIntent()
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    context.startActivity(shareIntent)
}