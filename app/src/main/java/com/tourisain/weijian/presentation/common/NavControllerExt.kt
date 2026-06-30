package com.tourisain.weijian.presentation.common

import android.os.SystemClock
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.tourisain.weijian.util.ErrorReporter

fun NavController.navigateStable(route: String) {
    if (!NavigationClickGuard.accept(route)) return
    if (currentBackStackEntry?.destination?.route == route) return
    runCatching {
        navigate(route) {
            launchSingleTop = true
        }
    }.onFailure { error ->
        NavigationClickGuard.reset()
        ErrorReporter.reportException(context, error)
    }
}

fun NavController.navigateTopLevelStable(route: String) {
    if (!NavigationClickGuard.accept(route)) return
    if (currentBackStackEntry?.destination?.route == route) return
    runCatching {
        navigate(route) {
            popUpTo(graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }.onFailure { error ->
        NavigationClickGuard.reset()
        ErrorReporter.reportException(context, error)
    }
}

fun NavController.popBackStackStable(fallbackRoute: String? = null) {
    runCatching { popBackStack() }
        .getOrDefault(false)
        .takeIf { it }
        ?: fallbackRoute?.let { navigateStable(it) }
}

private object NavigationClickGuard {
    private const val MIN_NAVIGATION_INTERVAL_MS = 450L
    private var lastRoute: String = ""
    private var lastAt: Long = 0L

    fun accept(route: String): Boolean {
        val now = SystemClock.elapsedRealtime()
        val accept = route != lastRoute || now - lastAt >= MIN_NAVIGATION_INTERVAL_MS
        if (accept) {
            lastRoute = route
            lastAt = now
        }
        return accept
    }

    fun reset() {
        lastRoute = ""
        lastAt = 0L
    }
}
