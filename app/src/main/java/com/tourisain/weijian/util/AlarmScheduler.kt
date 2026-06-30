package com.tourisain.weijian.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tourisain.weijian.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(
        id: String,
        timeInMillis: Long,
        message: String,
        notifySound: Boolean? = null,
        notifyVibrate: Boolean? = null,
        ringtoneUri: String? = null
    ) {
        val pendingIntent = buildPendingIntent(id, message, timeInMillis, notifySound, notifyVibrate, ringtoneUri)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms() -> {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
            else -> {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        }
    }

    fun cancel(id: String) {
        alarmManager.cancel(buildPendingIntent(id))
    }

    private fun buildPendingIntent(
        id: String,
        message: String? = null,
        timeInMillis: Long? = null,
        notifySound: Boolean? = null,
        notifyVibrate: Boolean? = null,
        ringtoneUri: String? = null
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_TITLE", context.getString(com.tourisain.weijian.R.string.app_name))
            message?.let { putExtra("EXTRA_MESSAGE", it) }
            putExtra("EXTRA_ID", id)
            timeInMillis?.let { putExtra("trigger_at", it) }
            notifySound?.let { putExtra("notify_sound", it) }
            notifyVibrate?.let { putExtra("notify_vibrate", it) }
            ringtoneUri?.let { putExtra("ringtone_uri", it) }
        }
        return PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
