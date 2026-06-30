package com.tourisain.weijian.presentation.profile

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MembershipPurchaseDialogTest {
    @Test
    fun purchaseDialogKeepsLongContactTextScrollable() {
        val source = resolveProjectFile("src/main/java/com/tourisain/weijian/presentation/profile/MembershipScreen.kt")
            .readText()

        assertTrue(source.contains("MEMBERSHIP_PURCHASE_DIALOG_TEXT_MAX_HEIGHT_DP"))
        assertTrue(source.contains(".heightIn(max = MEMBERSHIP_PURCHASE_DIALOG_TEXT_MAX_HEIGHT_DP.dp)"))
        assertTrue(source.contains(".verticalScroll(rememberScrollState())"))
    }

    private fun resolveProjectFile(path: String): File {
        return listOf(File(path), File("app/$path"))
            .firstOrNull { it.isFile }
            ?: error("Missing test fixture file: $path")
    }
}
