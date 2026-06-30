package com.tourisain.weijian.presentation.common

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String) {
    val items = listOf(
        Screen.Dashboard to (Lucide.House to "Home"),
        Screen.NoteList to (Lucide.StickyNote to "Notes"),
        Screen.AccountList to (Lucide.Wallet to "Accounts"),
        Screen.Settings to (Lucide.Settings to "Settings")
    )

    NavigationBar {
        items.forEach { (screen, iconAndLabel) ->
            val (icon, label) = iconAndLabel
            val route = when (screen) {
                is Screen.NoteList -> Screen.NotesAll.route
                else -> screen.route
            }
            NavigationBarItem(
                selected = currentRoute == screen.route || currentRoute == route,
                onClick = {
                    if (currentRoute != screen.route && currentRoute != route) {
                        navController.navigateTopLevelStable(route)
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}
