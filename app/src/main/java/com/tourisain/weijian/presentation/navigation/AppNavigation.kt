package com.tourisain.weijian.presentation.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tourisain.weijian.presentation.common.ErrorBoundary
import com.tourisain.weijian.presentation.common.popBackStackStable
import com.tourisain.weijian.presentation.dashboard.DashboardScreen
import com.tourisain.weijian.presentation.note.detail.NoteDetailScreen
import com.tourisain.weijian.presentation.note.list.NoteListScreen
import com.tourisain.weijian.presentation.privacy.LockScreen
import com.tourisain.weijian.presentation.privacy.PrivacyViewModel
import com.tourisain.weijian.util.PremiumManager
import com.tourisain.weijian.util.ActivationCodeManager
import kotlinx.coroutines.delay

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object NotesAll : Screen("notes_all")
    object NotesPinned : Screen("notes_pinned")
    object NotesTodo : Screen("notes_todo")
    object NoteList : Screen("note_list/{categoryId}") {
        fun createRoute(categoryId: String) = "note_list/${Uri.encode(categoryId.ifBlank { "all" })}"
    }
    object NoteDetail : Screen("note/{noteId}?categoryId={categoryId}") {
        fun createRoute(noteId: String, categoryId: String? = null): String {
            val encodedNoteId = Uri.encode(noteId.ifBlank { "new" })
            val normalizedCategoryId = categoryId?.takeIf { it.isNotBlank() }
            return if (normalizedCategoryId != null) {
                "note/$encodedNoteId?categoryId=${Uri.encode(normalizedCategoryId)}"
            } else {
                "note/$encodedNoteId"
            }
        }
    }
    object Search : Screen("search")
    object Profile : Screen("profile")
    object AccountList : Screen("accounts")
    object AccountEdit : Screen("account/{recordId}") {
        fun createRoute(recordId: String) = "account/${Uri.encode(recordId.ifBlank { "new" })}"
    }
    object AccountStats : Screen("account_stats")
    object Lock : Screen("lock")
    object Settings : Screen("settings")
    object Backup : Screen("backup")
    object CategoryManagement : Screen("categories")
    object RecycleBin : Screen("recycle_bin")
    object Membership : Screen("membership")
    object LanguageSettings : Screen("language_settings")
    object AppLayoutSettings : Screen("app_layout_settings")
    object AppearanceSettings : Screen("appearance_settings")
    object BehaviorSettings : Screen("behavior_settings")
    object SecuritySettings : Screen("security_settings")
    object OfficialWebsites : Screen("official_websites")
    object AboutSoftware : Screen("about_software")
    object ComplianceCenter : Screen("compliance_center")
    object ComplianceDocument : Screen("compliance_document/{documentId}") {
        fun createRoute(documentId: String) = "compliance_document/$documentId"
    }
    object ErrorLogs : Screen("error_logs")
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    premiumManager: PremiumManager,
    activationCodeManager: ActivationCodeManager,
    routeTo: String? = null,
    onExitRequested: () -> Unit = {}
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val privacyViewModel: PrivacyViewModel = hiltViewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    ErrorBoundary {
        val startDestination by rememberSaveable {
            mutableStateOf(Screen.Dashboard.route)
        }

        BackHandler(enabled = currentRoute == Screen.Dashboard.route) {
            onExitRequested()
        }

        LaunchedEffect(Unit) {
            if (routeTo != null) {
                delay(200)
                try {
                    when {
                        routeTo.startsWith("note/") -> {
                            val noteId = routeTo.substringAfter("note/")
                            navController.navigateSingleTopSafely("note/$noteId", context)
                        }
                        routeTo == "note" -> {
                            navController.navigateSingleTopSafely(Screen.NotesAll.route, context)
                        }
                        routeTo.startsWith("account/") -> {
                            val recordId = routeTo.substringAfter("account/")
                            navController.navigateSingleTopSafely("account/$recordId", context)
                        }
                        routeTo == "account" -> {
                            navController.navigateSingleTopSafely("accounts", context)
                        }
                    }
                } catch (e: Exception) {
                    com.tourisain.weijian.util.ErrorReporter.reportException(context, e)
                }
            }
        }

        NavHost(
            navController = navController, 
            startDestination = startDestination,
            enterTransition = { fadeIn(animationSpec = tween(675)) },
            exitTransition = { fadeOut(animationSpec = tween(675)) }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController)
            }
            
            composable(Screen.Settings.route) {
                com.tourisain.weijian.presentation.settings.SettingsScreen(navController)
            }

            composable(Screen.Backup.route) {
                com.tourisain.weijian.presentation.backup.BackupScreen(navController)
            }

            composable(Screen.Profile.route) {
                com.tourisain.weijian.presentation.profile.ProfileScreen(navController)
            }

            composable(Screen.Lock.route) {
                LockScreen(navController, privacyViewModel)
            }

            composable(Screen.NotesAll.route) {
                ErrorBoundary {
                    NoteListScreen(
                        navController = navController,
                        categoryId = "all"
                    )
                }
            }

            composable(Screen.NotesPinned.route) {
                ErrorBoundary {
                    NoteListScreen(
                        navController = navController,
                        categoryId = "all",
                        smartFilter = "pinned"
                    )
                }
            }

            composable(Screen.NotesTodo.route) {
                ErrorBoundary {
                    NoteListScreen(
                        navController = navController,
                        categoryId = "all",
                        smartFilter = "todo"
                    )
                }
            }

            composable(
                route = Screen.NoteList.route,
                arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
            ) { backStackEntry ->
                ErrorBoundary {
                    NoteListScreen(
                        navController = navController,
                        categoryId = backStackEntry.arguments?.getString("categoryId")
                    )
                }
            }

            composable(Screen.Search.route) {
                com.tourisain.weijian.presentation.search.SearchScreen(navController)
            }

            composable(
                route = Screen.NoteDetail.route,
                arguments = listOf(
                    navArgument("noteId") { type = NavType.StringType },
                    navArgument("categoryId") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                ErrorBoundary {
                    val noteId = backStackEntry.arguments?.getString("noteId")
                    NoteDetailScreen(navController, noteId)
                }
            }

            composable(Screen.AccountList.route) {
                ErrorBoundary {
                    com.tourisain.weijian.presentation.account.list.AccountListScreen(navController)
                }
            }

            composable(
                route = Screen.AccountEdit.route,
                arguments = listOf(navArgument("recordId") { type = NavType.StringType })
            ) {
                ErrorBoundary {
                    com.tourisain.weijian.presentation.account.edit.AccountEditScreen(navController)
                }
            }

            composable(Screen.AccountStats.route) {
                com.tourisain.weijian.presentation.account.stats.AccountStatsScreen(navController)
            }

            composable(Screen.CategoryManagement.route) {
                com.tourisain.weijian.presentation.category.CategoryManagementScreen(navController)
            }

            composable(Screen.RecycleBin.route) {
                ErrorBoundary {
                    com.tourisain.weijian.presentation.recyclebin.RecycleBinScreen(navController)
                }
            }

            composable(Screen.Membership.route) {
                com.tourisain.weijian.presentation.profile.MembershipScreen(
                    navController = navController, 
                    premiumManager = premiumManager,
                    activationCodeManager = activationCodeManager
                )
            }

            composable(Screen.LanguageSettings.route) {
                com.tourisain.weijian.presentation.settings.LanguageSettingsScreen(navController)
            }

            composable(Screen.AppLayoutSettings.route) {
                com.tourisain.weijian.presentation.settings.AppLayoutSettingsScreen(navController)
            }

            composable(Screen.AppearanceSettings.route) {
                com.tourisain.weijian.presentation.settings.AppearanceSettingsScreen(navController)
            }

            composable(Screen.BehaviorSettings.route) {
                com.tourisain.weijian.presentation.settings.BehaviorSettingsScreen(navController)
            }

            composable(Screen.SecuritySettings.route) {
                com.tourisain.weijian.presentation.settings.SecuritySettingsScreen(navController)
            }

            composable(Screen.OfficialWebsites.route) {
                com.tourisain.weijian.presentation.settings.OfficialWebsitesScreen(navController)
            }

            composable(Screen.AboutSoftware.route) {
                com.tourisain.weijian.presentation.settings.AboutSoftwareScreen(navController)
            }

            composable(Screen.ComplianceCenter.route) {
                com.tourisain.weijian.presentation.privacy.ComplianceCenterScreen(navController)
            }

            composable(
                route = Screen.ComplianceDocument.route,
                arguments = listOf(navArgument("documentId") { type = NavType.StringType })
            ) { backStackEntry ->
                com.tourisain.weijian.presentation.privacy.ComplianceDocumentScreen(
                    navController = navController,
                    documentId = backStackEntry.arguments?.getString("documentId") ?: "privacy"
                )
            }

            composable(Screen.ErrorLogs.route) {
                com.tourisain.weijian.presentation.errorlog.ErrorLogScreen(navController)
            }
        }
    }
}

private fun NavHostController.navigateSingleTopSafely(route: String, context: Context) {
    runCatching {
        navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
    }.onFailure { error ->
        com.tourisain.weijian.util.ErrorReporter.reportException(context, error)
    }
}

