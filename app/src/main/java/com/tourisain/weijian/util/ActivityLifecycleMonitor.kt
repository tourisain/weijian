package com.tourisain.weijian.util

import android.app.Activity
import android.app.Application
import android.os.Bundle

class ActivityLifecycleMonitor private constructor() : Application.ActivityLifecycleCallbacks {
    companion object {
        private var instance: ActivityLifecycleMonitor? = null
        private var currentActivity: Activity? = null
        fun getInstance(): ActivityLifecycleMonitor = instance ?: ActivityLifecycleMonitor().also { instance = it }
        fun init(application: Application) {
            application.unregisterActivityLifecycleCallbacks(getInstance())
            application.registerActivityLifecycleCallbacks(getInstance())
        }
        fun getCurrentActivity(): Activity? = currentActivity
    }
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) { currentActivity = activity }
    override fun onActivityStarted(activity: Activity) { currentActivity = activity }
    override fun onActivityResumed(activity: Activity) { currentActivity = activity }
    override fun onActivityPaused(activity: Activity) { if (currentActivity === activity) currentActivity = null }
    override fun onActivityStopped(activity: Activity) { if (currentActivity === activity) currentActivity = null }
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) { if (currentActivity === activity) currentActivity = null }
}
