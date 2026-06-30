package com.tourisain.weijian.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetQuickActionTest {
    @Test
    fun widgetQuickActionRoutesOpenCreationFlows() {
        assertEquals("note/new?categoryId=all", WidgetQuickAction.NewNote.routeTo)
        assertEquals("account/new", WidgetQuickAction.NewAccount.routeTo)
    }
}
