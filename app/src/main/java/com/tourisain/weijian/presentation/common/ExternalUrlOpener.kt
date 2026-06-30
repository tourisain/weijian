package com.tourisain.weijian.presentation.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.tourisain.weijian.R
import com.tourisain.weijian.util.ErrorReporter

fun Context.openExternalUrlStable(url: String): Boolean {
    return runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        true
    }.getOrElse { throwable ->
        ErrorReporter.reportException(this, throwable)
        Toast.makeText(this, getString(R.string.open_website_failed), Toast.LENGTH_SHORT).show()
        false
    }
}
