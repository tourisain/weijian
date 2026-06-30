package com.tourisain.weijian.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.tourisain.weijian.MainActivity
import com.tourisain.weijian.R

enum class WidgetQuickAction(val routeTo: String) {
    NewNote("note/new?categoryId=all"),
    NewAccount("account/new")
}

internal object WidgetUpdateHelper {
    fun updateStaticWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        layoutId: Int
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, layoutId)
            bindQuickActions(context, views, layoutId, appWidgetId)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun bindQuickActions(
        context: Context,
        views: RemoteViews,
        layoutId: Int,
        appWidgetId: Int
    ) {
        when (layoutId) {
            R.layout.widget_note -> {
                views.setOnClickPendingIntent(
                    R.id.widget_add_button,
                    quickActionPendingIntent(context, WidgetQuickAction.NewNote, appWidgetId)
                )
                views.setOnClickPendingIntent(
                    R.id.widget_container,
                    quickActionPendingIntent(context, WidgetQuickAction.NewNote, appWidgetId + 100_000)
                )
            }
            R.layout.widget_account -> {
                views.setOnClickPendingIntent(
                    R.id.add_account_button,
                    quickActionPendingIntent(context, WidgetQuickAction.NewAccount, appWidgetId + 200_000)
                )
                views.setOnClickPendingIntent(
                    R.id.account_container,
                    quickActionPendingIntent(context, WidgetQuickAction.NewAccount, appWidgetId + 300_000)
                )
            }
        }
    }

    private fun quickActionPendingIntent(
        context: Context,
        action: WidgetQuickAction,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route_to", action.routeTo)
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
