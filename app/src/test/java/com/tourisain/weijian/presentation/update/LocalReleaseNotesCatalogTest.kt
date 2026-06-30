package com.tourisain.weijian.presentation.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalReleaseNotesCatalogTest {
    @Test
    fun currentWidgetTodoAndBackupHealthReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20158, language = "zh")

        assertEquals(20158, release.versionCode)
        assertEquals("2.0.158", release.versionName)
        assertTrue(release.notes.any { it.contains("\u5f85\u529e\u7b5b\u9009") })
        assertTrue(release.notes.any { it.contains("\u5c0f\u7ec4\u4ef6") })
        assertTrue(release.notes.any { it.contains("\u5907\u4efd\u5065\u5eb7") })
    }

    @Test
    fun attractionReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20156, language = "zh")

        assertEquals(20156, release.versionCode)
        assertEquals("2.0.156", release.versionName)
        assertTrue(release.notes.any { it.contains("首页空状态") })
        assertTrue(release.notes.any { it.contains("待办") })
        assertTrue(release.notes.any { it.contains("应用商店") })
    }

    @Test
    fun fiveCutsReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20155, language = "zh")

        assertEquals(20155, release.versionCode)
        assertEquals("2.0.155", release.versionName)
        assertTrue(release.notes.any { it.contains("记账解析") })
        assertTrue(release.notes.any { it.contains("权限") })
        assertTrue(release.notes.any { it.contains("质量门禁") })
    }

    @Test
    fun focusedCleanupReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20154, language = "zh")

        assertEquals(20154, release.versionCode)
        assertEquals("2.0.154", release.versionName)
        assertTrue(release.notes.any { it.contains("编辑器") })
        assertTrue(release.notes.any { it.contains("Worker") })
        assertTrue(release.notes.any { it.contains("权限说明") })
    }

    @Test
    fun cleanupAndWebDavReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20153, language = "zh")

        assertEquals(20153, release.versionCode)
        assertEquals("2.0.153", release.versionName)
        assertTrue(release.notes.any { it.contains("Android ID") })
        assertTrue(release.notes.any { it.contains("WebDAV") })
        assertTrue(release.notes.any { it.contains("隐私政策") })
    }

    @Test
    fun exactUpdateDialogCatalogReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20152, language = "zh")

        assertEquals(20152, release.versionCode)
        assertEquals("2.0.152", release.versionName)
        assertTrue(release.notes.any { it.contains("\u66f4\u65b0\u5f39\u7a97") })
        assertTrue(release.notes.any { it.contains("\u6062\u590d\u524d\u9884\u89c8") })
    }

    @Test
    fun backupPreviewAndComplianceReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20151, language = "zh")

        assertEquals(20151, release.versionCode)
        assertEquals("2.0.151", release.versionName)
        assertTrue(release.notes.any { it.contains("\u9690\u79c1\u5408\u89c4") })
        assertTrue(release.notes.any { it.contains("\u8d85\u957f\u7b14\u8bb0") })
        assertTrue(release.notes.any { it.contains("\u6062\u590d\u524d\u9884\u89c8") })
    }

    @Test
    fun stabilityAndComplianceReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20150, language = "zh")

        assertEquals(20150, release.versionCode)
        assertEquals("2.0.150", release.versionName)
        assertTrue(release.notes.any { it.contains("安全环境") })
        assertTrue(release.notes.any { it.contains("详情页") })
        assertTrue(release.notes.any { it.contains("质量门禁") })
    }

    @Test
    fun coreCleanupReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20149, language = "zh")

        assertEquals(20149, release.versionCode)
        assertEquals("2.0.149", release.versionName)
        assertTrue(release.notes.any { it.contains("破坏性迁移") })
        assertTrue(release.notes.any { it.contains("旧功能") })
    }

    @Test
    fun feedbackChannelReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20148, language = "zh")

        assertEquals(20148, release.versionCode)
        assertEquals("2.0.148", release.versionName)
        assertTrue(release.notes.any { it.contains("用户反馈") })
        assertTrue(release.notes.any { it.contains("邮箱") })
    }

    @Test
    fun numberedListWritingReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20147, language = "zh")

        assertEquals(20147, release.versionCode)
        assertEquals("2.0.147", release.versionName)
        assertTrue(release.notes.any { it.contains("编号列表") })
        assertTrue(release.notes.any { it.contains("书写稳定性") })
    }

    @Test
    fun membershipAppearanceReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20146, language = "zh")

        assertEquals(20146, release.versionCode)
        assertEquals("2.0.146", release.versionName)
        assertTrue(release.notes.any { it.contains("会员") })
        assertTrue(release.notes.any { it.contains("动态取色") })
    }

    @Test
    fun keyboardAvoidanceReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20145, language = "zh")

        assertEquals(20145, release.versionCode)
        assertEquals("2.0.145", release.versionName)
        assertTrue(release.notes.any { it.contains("工具栏") })
        assertTrue(release.notes.any { it.contains("盲打") })
    }

    @Test
    fun writingPolishReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20144, language = "zh")

        assertEquals(20144, release.versionCode)
        assertEquals("2.0.144", release.versionName)
        assertTrue(release.notes.any { it.contains("空白行") })
        assertTrue(release.notes.any { it.contains("低内存") })
    }

    @Test
    fun coreResourceCleanupReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20143, language = "zh")

        assertEquals(20143, release.versionCode)
        assertEquals("2.0.143", release.versionName)
        assertTrue(release.notes.any { it.contains("旧功能文案") })
        assertTrue(release.notes.any { it.contains("资源范围测试") })
    }

    @Test
    fun resourceCleanupReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20142, language = "zh")

        assertEquals(20142, release.versionCode)
        assertEquals("2.0.142", release.versionName)
        assertTrue(release.notes.any { it.contains("资源文案") })
        assertTrue(release.notes.any { it.contains("收支分析") })
    }

    @Test
    fun cleanupReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20141, language = "zh")

        assertEquals(20141, release.versionCode)
        assertEquals("2.0.141", release.versionName)
        assertTrue(release.notes.any { it.contains("三刀收缩") })
        assertTrue(release.notes.any { it.contains("备份范围") })
    }

    @Test
    fun editorStateMachineReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20140, language = "zh")

        assertEquals(20140, release.versionCode)
        assertEquals("2.0.140", release.versionName)
        assertTrue(release.notes.any { it.contains("\u72b6\u6001") })
        assertTrue(release.notes.any { it.contains("\u8d85\u957f\u6587\u672c") })
    }

    @Test
    fun editorSmoothnessReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20139, language = "zh")

        assertEquals(20139, release.versionCode)
        assertEquals("2.0.139", release.versionName)
        assertTrue(release.notes.any { it.contains("\u8f93\u5165") })
        assertTrue(release.notes.any { it.contains("\u5bcc\u6587\u672c") })
    }

    @Test
    fun visualPolishReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20138, language = "zh")

        assertEquals(20138, release.versionCode)
        assertEquals("2.0.138", release.versionName)
        assertTrue(release.notes.any { it.contains("\u5168\u5c40\u89c6\u89c9") })
        assertTrue(release.notes.any { it.contains("\u7b14\u8bb0\u5217\u8868") })
    }

    @Test
    fun currentReleaseUsesExactVersionNotes() {
        val release = localReleaseNotesFor(versionCode = 20137, language = "zh")

        assertEquals(20137, release.versionCode)
        assertEquals("2.0.137", release.versionName)
        assertTrue(release.notes.any { it.contains("\u66f4\u65b0\u5f39\u7a97") })
        assertTrue(release.notes.any { it.contains("\u6781\u7b80\u5199\u4f5c") })
    }

    @Test
    fun releaseNotesAreLocalizedForSupportedLanguages() {
        val zh = localReleaseNotesFor(versionCode = 20137, language = "zh")
        val en = localReleaseNotesFor(versionCode = 20137, language = "en")
        val fr = localReleaseNotesFor(versionCode = 20137, language = "fr")

        assertFalse(zh.notes.first() == en.notes.first())
        assertFalse(en.notes.first() == fr.notes.first())
        assertTrue(en.notes.any { it.contains("release notes", ignoreCase = true) })
        assertTrue(fr.notes.any { it.contains("notes de version", ignoreCase = true) })
    }

    @Test
    fun unknownVersionFallsBackToClosestKnownRelease() {
        val release = localReleaseNotesFor(versionCode = 99999, language = "zh")

        assertEquals(20158, release.versionCode)
        assertEquals("2.0.158", release.versionName)
        assertTrue(release.notes.isNotEmpty())
    }
}
