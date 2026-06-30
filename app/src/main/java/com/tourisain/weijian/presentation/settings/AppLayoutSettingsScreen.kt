package com.tourisain.weijian.presentation.settings

import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.icons.Lucide

@Composable
fun AppLayoutSettingsScreen(
    navController: NavController,
    viewModel: AppLayoutViewModel = hiltViewModel()
) {
    val sidebarItems by viewModel.sidebarItems.collectAsStateWithLifecycle()
    val cardVisibility by viewModel.cardVisibility.collectAsStateWithLifecycle()

    SimpleSettingsPage(navController, stringResource(R.string.app_layout_settings)) {
        SettingsGroup {
            SettingItem(
                icon = Lucide.LayoutGrid,
                title = stringResource(R.string.layout_home_entries),
                subtitle = stringResource(R.string.layout_home_entries_desc),
                trailingContent = { Text("${sidebarItems.count { it.visible }}/${sidebarItems.size}") }
            )
            sidebarItems.forEach { item ->
                SettingsDivider()
                SettingItem(
                    title = routeTitle(item.key, item.name),
                    subtitle = routeDescription(item.key),
                    trailingContent = {
                        Switch(
                            checked = item.visible,
                            onCheckedChange = { viewModel.toggleSidebarItemVisibility(item.key) }
                        )
                    }
                )
            }
        }

        SettingsGroup {
            SettingItem(
                icon = Lucide.StickyNote,
                title = stringResource(R.string.layout_note_card),
                subtitle = stringResource(R.string.layout_note_card_desc),
                trailingContent = {
                    Switch(
                        checked = cardVisibility.getOrDefault("note", true),
                        onCheckedChange = { viewModel.toggleCardVisibility("note") }
                    )
                }
            )
            SettingsDivider()
            SettingItem(
                icon = Lucide.Wallet,
                title = stringResource(R.string.layout_account_card),
                subtitle = stringResource(R.string.layout_account_card_desc),
                trailingContent = {
                    Switch(
                        checked = cardVisibility.getOrDefault("account", true),
                        onCheckedChange = { viewModel.toggleCardVisibility("account") }
                    )
                }
            )
        }
    }
}

@Composable
private fun routeTitle(route: String, fallback: String): String = when (route) {
    "note_list/default" -> stringResource(R.string.notes_title)
    "accounts" -> stringResource(R.string.accounting)
    "search" -> stringResource(R.string.search)
    "settings" -> stringResource(R.string.settings)
    else -> fallback
}

@Composable
private fun routeDescription(route: String): String = when (route) {
    "note_list/default" -> stringResource(R.string.layout_route_notes)
    "accounts" -> stringResource(R.string.layout_route_accounts)
    "search" -> stringResource(R.string.layout_route_search)
    "settings" -> stringResource(R.string.layout_route_settings)
    else -> route
}
