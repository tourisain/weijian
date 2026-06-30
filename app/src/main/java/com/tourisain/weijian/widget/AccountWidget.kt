package com.tourisain.weijian.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.tourisain.weijian.R

class AccountWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdateHelper.updateStaticWidget(context, appWidgetManager, appWidgetIds, R.layout.widget_account)
    }
}
