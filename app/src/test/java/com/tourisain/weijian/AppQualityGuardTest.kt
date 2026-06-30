package com.tourisain.weijian

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppQualityGuardTest {
    @Test
    fun startupCodeDoesNotContainMojibakeEmptyDelayedTasksOrForcedGc() {
        val startupText = listOf(
            "src/main/java/com/tourisain/weijian/MemoApplication.kt",
            "src/main/java/com/tourisain/weijian/MainActivity.kt"
        ).joinToString("\n") { resolveProjectFile(it).readText() }

        listOf("鍒", "绔", "闈", "涓", "搴", "瑷", "篣").forEach { token ->
            assertFalse("Startup source should not contain mojibake token: $token", startupText.contains(token))
        }
        assertFalse("Startup source should not force garbage collection", startupText.contains("System.gc()"))
        assertFalse("Removed feature placeholder delay should not remain", startupText.contains("delay(3000)"))
        assertFalse("Removed feature placeholder should not remain", startupText.contains("initializeAchievementsAndChallenges"))
    }

    @Test
    fun securityChecksDoNotQueryOtherInstalledPackages() {
        val manifestText = resolveProjectFile("src/main/AndroidManifest.xml").readText()
        val securityText = resolveProjectFile("src/main/java/com/tourisain/weijian/util/SecurityEnvironmentMonitor.kt").readText()
        val privacyText = resolveProjectFile("src/main/java/com/tourisain/weijian/presentation/privacy/PrivacyPolicyScreen.kt").readText()

        assertFalse("Manifest should not declare package visibility queries", manifestText.contains("<queries>"))
        assertFalse("Security monitor should not keep package-list probes", securityText.contains("XPOSED_PACKAGES"))
        assertFalse("Security monitor should not query arbitrary package names", securityText.contains("getPackageInfo(packageName, 0)"))
        assertFalse("Privacy copy should not disclose installed-app checks after removing them", privacyText.contains("软件安装列表"))
        assertFalse("Privacy copy should not disclose installed-app checks after removing them", privacyText.contains("installed-app"))
    }

    @Test
    fun preConsentDeviceIdAccessStaysBehindExplicitGuards() {
        val backupText = resolveProjectFile("src/main/java/com/tourisain/weijian/data/repository/BackupRepository.kt").readText()
        val qualityScript = resolveRootFile("tools/quality_check.ps1").readText()

        assertTrue("Backup should use a harmless pre-consent user id", backupText.contains("PRE_CONSENT_USER_ID"))
        assertTrue("Backup should read privacy policy acceptance before any device id fallback", backupText.contains("isPrivacyPolicyAccepted.first()"))
        assertTrue("Backup should read user agreement acceptance before any device id fallback", backupText.contains("isUserAgreementAccepted.first()"))
        assertTrue(
            "Device id fallback should only run after both agreements are accepted",
            backupText.contains("if (hasRequiredConsent) DeviceUtil.getDeviceId(context) else PRE_CONSENT_USER_ID")
        )
        assertTrue("Quality gate should keep watching for pre-consent device-id regressions", qualityScript.contains("PRE_CONSENT_USER_ID"))
        assertTrue("Quality gate should keep watching direct Android ID access", qualityScript.contains("Settings.Secure.ANDROID_ID"))
    }

    @Test
    fun privacyPolicyCopyMatchesCurrentCoreFeatureScope() {
        val privacyText = resolveProjectFile("src/main/java/com/tourisain/weijian/presentation/privacy/PrivacyPolicyScreen.kt").readText()
        val qualityScript = resolveRootFile("tools/quality_check.ps1").readText()

        listOf(
            "\u65e5\u8bb0",
            "\u9884\u7b97",
            "\u5012\u6570\u65e5",
            "\u5f85\u529e"
        ).forEach { legacyScope ->
            assertFalse("Privacy policy should not disclose removed standalone scope: $legacyScope", privacyText.contains(legacyScope))
            assertTrue("Quality gate should watch removed privacy scope: $legacyScope", qualityScript.contains(legacyScope))
        }
    }

    @Test
    fun androidIdAccessIsCentralizedBehindDeviceUtil() {
        val mainFiles = resolveProjectPath("src/main/java/com/tourisain/weijian")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()
        val filesWithAndroidId = mainFiles
            .filter { it.readText().contains("Settings.Secure.ANDROID_ID") }
            .map { it.path.replace('\\', '/') }
            .sorted()

        assertEquals(
            "Only DeviceUtil should touch Android ID directly; callers must go through audited helpers",
            1,
            filesWithAndroidId.size
        )
        assertTrue(
            "Only DeviceUtil should touch Android ID directly; callers must go through audited helpers: $filesWithAndroidId",
            filesWithAndroidId.single().endsWith("com/tourisain/weijian/util/DeviceUtil.kt")
        )
    }

    @Test
    fun androidBackupPolicyIsLocalUserControlledOnly() {
        val manifestText = resolveProjectFile("src/main/AndroidManifest.xml").readText()
        val backupRules = resolveProjectFile("src/main/res/xml/backup_rules.xml").readText()
        val dataExtractionRules = resolveProjectFile("src/main/res/xml/data_extraction_rules.xml").readText()

        assertTrue("Platform backup should be disabled for local-first data", manifestText.contains("android:allowBackup=\"false\""))
        assertFalse("Full backup should not include all app root data", backupRules.contains("<include domain=\"root\""))
        assertTrue("Full backup should explicitly exclude root data", backupRules.contains("<exclude domain=\"root\""))
        assertTrue("Cloud backup should exclude root data", dataExtractionRules.contains("<exclude domain=\"root\""))
    }

    @Test
    fun settingsRowsOnlyShowClickAffordanceWhenActionExists() {
        val source = resolveProjectFile("src/main/java/com/tourisain/weijian/presentation/settings/SettingsComponents.kt").readText()

        assertTrue("SettingItem should allow a null click action", source.contains("onClick: (() -> Unit)? = null"))
        assertFalse(
            "SettingItem should not be unconditionally clickable",
            source.contains(".fillMaxWidth()\n            .clickable { onClick() }")
        )
        assertTrue(
            "SettingItem should apply click handling only when an action exists",
            source.contains(".then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)")
        )
        assertTrue("Rows with switches should toggle when the row is tapped", source.contains("onClick = { onCheckedChange(!checked) }"))
        assertTrue("Chevron should be tied to click affordance", source.contains("onClick != null"))
    }

    @Test
    fun noteDetailKeepsRichRenderingInFocusedFile() {
        val detailFile = resolveProjectFile("src/main/java/com/tourisain/weijian/presentation/note/detail/NoteDetailScreen.kt")
        val rendererFile = resolveProjectFile("src/main/java/com/tourisain/weijian/presentation/note/detail/RichNoteVisualTransformation.kt")
        val detailSource = detailFile.readText()
        val rendererSource = rendererFile.readText()

        assertTrue("Note detail screen should stay below a maintainable size", detailSource.lineSequence().count() < 1_000)
        assertFalse("Rich rendering implementation should not live inside the screen file", detailSource.contains("class RichNoteRenderer"))
        assertTrue("Rich rendering implementation should live in its focused file", rendererSource.contains("class RichNoteRenderer"))
        assertTrue("Rich marker detection should live with the renderer", rendererSource.contains("fun hasRichTextMarker"))
    }

    @Test
    fun repositoryHasLocalQualityGateForReleaseChecks() {
        val script = resolveRootFile("tools/quality_check.ps1")
        val gradleProperties = resolveRootFile("gradle.properties").readText()

        assertTrue("Local quality gate script should exist", script.isFile)
        val scriptText = script.readText()
        listOf(
            "fallbackToDestructiveMigration",
            "<queries>",
            "System.gc",
            "privacyInstalledAppsToken",
            "Markwon",
            "android:allowBackup",
            "parseAmountAndCurrencyClean",
            "AccountNoteParser.kt",
            "PermissionDisclosureScenario.Avatar",
            "editorContentBottomPaddingDp",
            "ocr_result_dialog.xml",
            "whats_new_current_notes"
        ).forEach { token ->
            assertTrue("Quality gate should check token: $token", scriptText.contains(token))
        }
        assertTrue(
            "compileSdk 36 support warning should be intentionally suppressed until AGP is upgraded",
            gradleProperties.contains("android.suppressUnsupportedCompileSdk=36")
        )
    }

    @Test
    fun focusedCleanupKeepsWorkersEditorAndLegacyMigrationLean() {
        val mainFiles = resolveProjectPath("src/main/java/com/tourisain/weijian")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()
        val sourcesByPath = mainFiles.associate { it.path.replace('\\', '/') to it.readText() }
        val allSource = sourcesByPath.values.joinToString("\n")

        assertEquals(
            "Backup work should have one worker implementation with mode-based branching",
            1,
            Regex("""class\s+BackupWorker\b""").findAll(allSource).count()
        )
        assertTrue(
            "Legacy cleanup migration should be explicitly named as legacy cleanup",
            allSource.contains("LEGACY_FEATURE_CLEANUP_MIGRATION_78_79")
        )
        assertFalse(
            "Old whole-content formatting entry should not duplicate the selection-aware editor toolbar",
            sourcesByPath.values.any { it.contains("fun formatText(") }
        )
        assertFalse(
            "Unused tag cache path should be removed from the note list flow",
            sourcesByPath.entries
                .filter { it.key.endsWith("presentation/note/list/NoteListViewModel.kt") || it.key.endsWith("data/repository/NoteRepository.kt") || it.key.endsWith("data/database/dao/NoteDao.kt") }
                .any { it.value.contains("fun getAllTags(") || it.value.contains("ConcurrentHashMap") }
        )
        assertTrue(
            "Unified permission disclosure model should be shared by file and network entry points",
            sourcesByPath.values.any { it.contains("PermissionDisclosureScenario") && it.contains("FileImport") && it.contains("WebDavNetwork") }
        )
    }

    @Test
    fun permissionDisclosuresAreWiredToSensitiveEntryPoints() {
        val profileSource = resolveProjectFile("src/main/java/com/tourisain/weijian/presentation/profile/ProfileScreen.kt").readText()
        val behaviorSource = resolveProjectFile("src/main/java/com/tourisain/weijian/presentation/settings/BehaviorSettingsScreen.kt").readText()
        val noteDetailSource = resolveProjectFile("src/main/java/com/tourisain/weijian/presentation/note/detail/NoteDetailScreen.kt").readText()
        val backupSource = resolveProjectFile("src/main/java/com/tourisain/weijian/presentation/backup/BackupScreen.kt").readText()

        assertTrue("Avatar file picker should use unified permission disclosure", profileSource.contains("PermissionDisclosureScenario.Avatar"))
        assertTrue("Reminder switch should use notification disclosure before enabling", behaviorSource.contains("PermissionDisclosureScenario.Notification"))
        assertTrue("Reminder switch should disclose exact alarm use before enabling", behaviorSource.contains("PermissionDisclosureScenario.ExactAlarm"))
        assertTrue("Attachment picker should use unified permission disclosure", noteDetailSource.contains("PermissionDisclosureScenario.Attachment"))
        assertTrue("Backup import should use unified permission disclosure", backupSource.contains("PermissionDisclosureScenario.FileImport"))
        assertTrue("Backup export should use unified permission disclosure", backupSource.contains("PermissionDisclosureScenario.FileExport"))
        assertTrue("WebDAV actions should use unified permission disclosure", backupSource.contains("PermissionDisclosureScenario.WebDavNetwork"))
    }

    @Test
    fun unusedLegacyFilesAndDrawingCopyStayDeleted() {
        listOf(
            "src/main/res/layout/ocr_result_dialog.xml",
            "src/main/java/com/tourisain/weijian/util/AvatarUtil.kt",
            "src/main/java/com/tourisain/weijian/util/AvatarFrameUtil.kt",
            "src/main/java/com/tourisain/weijian/data/model/AvatarFrame.kt",
            "src/main/java/com/tourisain/weijian/domain/model/Notification.kt",
            "src/main/java/com/tourisain/weijian/domain/repository/NotificationRepository.kt"
        ).forEach { path ->
            assertFalse("Unused legacy file should stay deleted: $path", resolveMaybeProjectFile(path).isFile)
        }

        val resourceText = listOf(
            "src/main/res/values/strings.xml",
            "src/main/res/values-en/strings.xml",
            "src/main/res/values-fr/strings.xml"
        ).joinToString("\n") { resolveProjectFile(it).readText() }
        assertFalse("Drawing feature copy should not remain", resourceText.contains("name=\"drawing\""))
        assertFalse("Drawing list copy should not remain", resourceText.contains("name=\"drawing_list\""))
    }

    private fun resolveProjectFile(path: String): File {
        return listOf(File(path), File("app/$path"))
            .firstOrNull { it.isFile }
            ?: error("Missing test fixture file: $path")
    }

    private fun resolveMaybeProjectFile(path: String): File {
        return listOf(File(path), File("app/$path")).firstOrNull { it.exists() } ?: File(path)
    }

    private fun resolveProjectPath(path: String): File {
        return listOf(File(path), File("app/$path"))
            .firstOrNull { it.exists() }
            ?: error("Missing test fixture path: $path")
    }

    private fun resolveRootFile(path: String): File {
        return listOf(File(path), File("../$path"))
            .firstOrNull { it.isFile }
            ?: error("Missing root fixture file: $path")
    }
}
