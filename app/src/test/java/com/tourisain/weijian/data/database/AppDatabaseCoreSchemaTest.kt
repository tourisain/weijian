package com.tourisain.weijian.data.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppDatabaseCoreSchemaTest {
    @Test
    fun databaseExposesOnlyCoreWeijianDaos() {
        val daoMethodNames = AppDatabase::class.java.declaredMethods
            .map { it.name }
            .filter { it.endsWith("Dao") }
            .sorted()

        assertEquals(
            listOf(
                "accountDao",
                "categoryDao",
                "noteCategoryDao",
                "noteDao",
                "noteRevisionDao",
                "userDao"
            ).sorted(),
            daoMethodNames
        )
    }

    @Test
    fun databaseConfigurationDoesNotAllowSilentDataWipe() {
        val databaseSource = resolveProjectFile("src/main/java/com/tourisain/weijian/data/database/AppDatabase.kt").readText()
        val moduleSource = resolveProjectFile("src/main/java/com/tourisain/weijian/di/AppModule.kt").readText()
        val buildSource = resolveProjectFile("app/build.gradle.kts").readText()

        assertFalse(databaseSource.contains("fallbackToDestructiveMigration()"))
        assertFalse(moduleSource.contains("fallbackToDestructiveMigration()"))
        assertTrue(databaseSource.contains("exportSchema = true"))
        assertTrue(buildSource.contains("room.schemaLocation"))
    }

    @Test
    fun databaseHasSingleProductionBuilder() {
        assertFalse(resolveProjectFile("src/main/java/com/tourisain/weijian/data/database/DatabaseManager.kt").exists())
    }

    private fun resolveProjectFile(path: String): File {
        return listOf(
            File(path),
            File("app/$path"),
            File("../$path"),
            File("../app/$path")
        )
            .firstOrNull { it.exists() }
            ?: File("app/$path")
    }
}
