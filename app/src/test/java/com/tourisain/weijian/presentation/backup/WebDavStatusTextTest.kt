package com.tourisain.weijian.presentation.backup

import org.junit.Assert.assertEquals
import org.junit.Test

class WebDavStatusTextTest {
    private val labels = WebDavStatusLabels(
        notConfigured = "Not configured",
        checking = "Checking",
        configured = "Configured",
        autoEnabled = "Auto on",
        autoDisabled = "Auto off"
    )

    @Test
    fun emptyUrlShowsNotConfigured() {
        val subtitle = webDavStatusSubtitle(
            status = WebDavStatusUiState(),
            url = "",
            isAutoBackupEnabled = false,
            labels = labels
        )

        assertEquals("Not configured", subtitle)
    }

    @Test
    fun checkingStateTakesPriority() {
        val subtitle = webDavStatusSubtitle(
            status = WebDavStatusUiState(isChecking = true, message = "Previous"),
            url = "https://example.com/dav",
            isAutoBackupEnabled = true,
            labels = labels
        )

        assertEquals("Checking", subtitle)
    }

    @Test
    fun explicitMessageTakesPriorityAfterUrl() {
        val subtitle = webDavStatusSubtitle(
            status = WebDavStatusUiState(message = "Connected"),
            url = "https://example.com/dav",
            isAutoBackupEnabled = true,
            labels = labels
        )

        assertEquals("Connected", subtitle)
    }

    @Test
    fun configuredUrlFallsBackToAutoState() {
        assertEquals(
            "Configured - Auto on",
            webDavStatusSubtitle(
                status = WebDavStatusUiState(),
                url = "https://example.com/dav",
                isAutoBackupEnabled = true,
                labels = labels
            )
        )
        assertEquals(
            "Configured - Auto off",
            webDavStatusSubtitle(
                status = WebDavStatusUiState(),
                url = "https://example.com/dav",
                isAutoBackupEnabled = false,
                labels = labels
            )
        )
    }
}
