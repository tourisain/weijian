package com.tourisain.weijian.data.model

import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class CoreResourceScopeTest {
    @Test
    fun noteDetailCopyUsesStringResourcesForVisibleLabels() {
        val source = resolveProjectFile("src/main/java/com/tourisain/weijian/presentation/note/detail/NoteDetailScreen.kt")
            .readText()

        assertFalse(source.contains("text = \"信息\""))
        assertFalse(source.contains("title = { Text(\"信息\") }"))
    }

    @Test
    fun stringResourcesDoNotExposeRemovedFeatureCopy() {
        val resourceText = listOf(
            "src/main/res/values/strings.xml",
            "src/main/res/values-en/strings.xml",
            "src/main/res/values-fr/strings.xml"
        ).joinToString("\n") { path ->
            resolveProjectFile(path).readText()
        }

        val removedTokens = listOf(
            "sms_read_enabled",
            "sms_read_permission_denied",
            "notification_reading",
            "sms_reading",
            "no_travel_memories",
            "create_first_travel_memory",
            "create_travel_memory",
            "edit_travel_memory",
            "time_capsule",
            "create_time_capsule",
            "unopened_capsules",
            "opened_capsules",
            "capsules_will_appear",
            "travel_memory",
            "map_not_available",
            "need_tencent_map",
            "view_map",
            "diary content",
            "日记内容",
            "todo reminders",
            "待办提醒",
            "Usage Data",
            "使用数据",
            "secure servers",
            "安全服务器",
            "advanced_pomodoro",
            "study_room",
            "theme_store",
            "team_notification",
            "no_team_notifications",
            "高级番茄钟",
            "自习室",
            "主题商店",
            "团队通知"
        )

        removedTokens.forEach { token ->
            assertFalse("Removed resource copy should not remain: $token", resourceText.contains(token))
        }
    }

    @Test
    fun unusedPresentationLibrariesAreNotDeclaredOrDisclosed() {
        val buildText = resolveProjectFile("build.gradle.kts").readText()
        val proguardText = resolveProjectFile("proguard-rules.pro").readText()
        val privacyText = resolveProjectFile("src/main/java/com/tourisain/weijian/presentation/privacy/PrivacyPolicyScreen.kt").readText()

        val unusedTokens = listOf(
            "io.noties.markwon",
            "PhotoView",
            "MPAndroidChart",
            "com.tourisain.markdowneditor",
            "Markwon"
        )

        unusedTokens.forEach { token ->
            assertFalse("Unused library or legacy keep rule should not remain: $token", buildText.contains(token))
            assertFalse("Unused library or legacy keep rule should not remain: $token", proguardText.contains(token))
            assertFalse("Unused library should not be disclosed as active: $token", privacyText.contains(token))
        }
    }

    private fun resolveProjectFile(path: String): File {
        return listOf(File(path), File("app/$path"))
            .firstOrNull { it.isFile }
            ?: error("Missing test fixture file: $path")
    }
}
