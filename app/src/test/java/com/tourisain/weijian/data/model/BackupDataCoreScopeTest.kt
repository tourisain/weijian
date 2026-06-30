package com.tourisain.weijian.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BackupDataCoreScopeTest {
    @Test
    fun backupDataOnlyCarriesCoreRecordsAndSettings() {
        val propertyNames = BackupData::class.java.declaredFields
            .map { it.name }
            .filterNot { it.startsWith("$") }
            .sorted()

        assertEquals(
            listOf(
                "accountRecords",
                "appSettings",
                "categories",
                "cardVisibility",
                "noteCategories",
                "noteRevisions",
                "notes",
                "timestamp",
                "user",
                "version"
            ).sorted(),
            propertyNames
        )
    }
}
