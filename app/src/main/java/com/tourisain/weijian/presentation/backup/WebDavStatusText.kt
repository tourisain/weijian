package com.tourisain.weijian.presentation.backup

data class WebDavStatusUiState(
    val isChecking: Boolean = false,
    val lastCheckedAt: Long = 0L,
    val lastSuccess: Boolean? = null,
    val message: String = ""
)

data class WebDavStatusLabels(
    val notConfigured: String,
    val checking: String,
    val configured: String,
    val autoEnabled: String,
    val autoDisabled: String
)

fun webDavStatusSubtitle(
    status: WebDavStatusUiState,
    url: String,
    isAutoBackupEnabled: Boolean,
    labels: WebDavStatusLabels
): String {
    if (url.isBlank()) return labels.notConfigured
    if (status.isChecking) return labels.checking
    if (status.message.isNotBlank()) return status.message
    val autoLabel = if (isAutoBackupEnabled) labels.autoEnabled else labels.autoDisabled
    return "${labels.configured} - $autoLabel"
}
