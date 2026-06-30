package com.tourisain.weijian

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicAppearancePolicyTest {
    @Test
    fun dynamicAppearanceRequiresSupportAndClassicStyle() {
        assertTrue(shouldApplyDynamicAppearance(dynamicTheme = true, sdkInt = 31, styleMode = "classic"))
        assertFalse(shouldApplyDynamicAppearance(dynamicTheme = false, sdkInt = 31, styleMode = "classic"))
        assertFalse(shouldApplyDynamicAppearance(dynamicTheme = true, sdkInt = 30, styleMode = "classic"))
        assertFalse(shouldApplyDynamicAppearance(dynamicTheme = true, sdkInt = 31, styleMode = "ios"))
        assertFalse(shouldApplyDynamicAppearance(dynamicTheme = null, sdkInt = 31, styleMode = "classic"))
    }
}
