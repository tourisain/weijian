package com.tourisain.weijian.presentation.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DashboardFirstStartActionTest {
    @Test
    fun firstStartActionsCoverCoreCreationAndTrustEntries() {
        val actions = dashboardFirstStartActions(noteVisible = true, accountVisible = true)

        assertEquals(
            listOf(
                DashboardFirstStartAction.NewNote,
                DashboardFirstStartAction.NewAccount,
                DashboardFirstStartAction.CreateFolder,
                DashboardFirstStartAction.Backup
            ),
            actions.map { it.action }
        )
    }

    @Test
    fun firstStartActionsRespectHiddenHomeSections() {
        val actions = dashboardFirstStartActions(noteVisible = false, accountVisible = true)

        assertEquals(
            listOf(
                DashboardFirstStartAction.NewAccount,
                DashboardFirstStartAction.Backup
            ),
            actions.map { it.action }
        )
    }

    @Test
    fun recentActivitiesAreGroupedIntoTodayYesterdayAndEarlier() {
        val zone = TimeZone.getTimeZone("Asia/Shanghai")
        val now = calendarTime(zone, 2026, Calendar.JUNE, 28, 12, 0)
        val sections = dashboardActivitySections(
            activities = listOf(
                DashboardActivity(id = "today", timestamp = calendarTime(zone, 2026, Calendar.JUNE, 28, 9, 0)),
                DashboardActivity(id = "yesterday", timestamp = calendarTime(zone, 2026, Calendar.JUNE, 27, 23, 0)),
                DashboardActivity(id = "earlier", timestamp = calendarTime(zone, 2026, Calendar.JUNE, 23, 8, 0))
            ),
            now = now,
            timeZone = zone
        )

        assertEquals(
            listOf(
                DashboardActivitySectionKey.Today,
                DashboardActivitySectionKey.Yesterday,
                DashboardActivitySectionKey.Earlier
            ),
            sections.map { it.key }
        )
        assertEquals(listOf("today"), sections[0].activities.map { it.id })
        assertEquals(listOf("yesterday"), sections[1].activities.map { it.id })
        assertEquals(listOf("earlier"), sections[2].activities.map { it.id })
    }

    private fun calendarTime(
        zone: TimeZone,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): Long {
        return Calendar.getInstance(zone).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
