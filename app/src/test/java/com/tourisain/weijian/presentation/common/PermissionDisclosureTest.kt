package com.tourisain.weijian.presentation.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionDisclosureTest {
    @Test
    fun fileImportDisclosureUsesExplicitAcceptAndRejectLabels() {
        val disclosure = permissionDisclosureFor(PermissionDisclosureScenario.FileImport)

        assertEquals("同意并选择", disclosure.confirmLabel)
        assertEquals("拒绝并取消", disclosure.dismissLabel)
        assertTrue(disclosure.message.contains("用途"))
        assertTrue(disclosure.message.contains("拒绝后"))
    }

    @Test
    fun webDavDisclosureNamesNetworkUseAndUserConfiguredServer() {
        val disclosure = permissionDisclosureFor(PermissionDisclosureScenario.WebDavNetwork)

        assertTrue(disclosure.message.contains("WebDAV"))
        assertTrue(disclosure.message.contains("您配置"))
        assertEquals("同意并继续", disclosure.confirmLabel)
    }
}
