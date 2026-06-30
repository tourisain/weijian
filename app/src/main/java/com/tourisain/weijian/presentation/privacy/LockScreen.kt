package com.tourisain.weijian.presentation.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle

@Composable
fun LockScreen(navController: NavController, viewModel: PrivacyViewModel) {
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
    LaunchedEffect(isUnlocked) {
        if (isUnlocked) {
            runCatching {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Lock.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
    PrivacyKeypad(viewModel = viewModel)
}

@Composable
fun PrivacyKeypad(viewModel: PrivacyViewModel) {
    val inputPassword by viewModel.inputPassword.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val passwordLength by viewModel.passwordLength.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleNotesStyle.Background)
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(color = AppleNotesStyle.AccentSoft, shape = CircleShape) {
            Box(modifier = Modifier.size(76.dp), contentAlignment = Alignment.Center) {
                Icon(
                    Lucide.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = AppleNotesStyle.Accent
                )
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = stringResource(R.string.privacy_unlock_title),
            color = AppleNotesStyle.PrimaryText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.privacy_unlock_subtitle),
            color = AppleNotesStyle.SecondaryText,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(passwordLength.coerceIn(3, 6)) { index ->
                Surface(
                    color = if (index < inputPassword.length) AppleNotesStyle.Accent else AppleNotesStyle.SearchSurface,
                    shape = CircleShape,
                    modifier = Modifier.size(13.dp)
                ) {}
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (error == null) " " else stringResource(R.string.privacy_wrong_password),
            color = AppleNotesStyle.Destructive,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(24.dp))
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "delete")
        )
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(vertical = 5.dp)) {
                row.forEach { key ->
                    when (key) {
                        "" -> Spacer(modifier = Modifier.size(72.dp))
                        "delete" -> KeyButton(onClick = { viewModel.onPasswordChange(inputPassword.dropLast(1)) }) {
                            Icon(
                                Lucide.Eraser,
                                contentDescription = stringResource(R.string.privacy_delete_digit),
                                tint = AppleNotesStyle.PrimaryText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        else -> KeyButton(onClick = { viewModel.onPasswordChange(inputPassword + key) }) {
                            Text(
                                text = key,
                                color = AppleNotesStyle.PrimaryText,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(color = AppleNotesStyle.Surface, shape = CircleShape, modifier = Modifier.size(72.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
