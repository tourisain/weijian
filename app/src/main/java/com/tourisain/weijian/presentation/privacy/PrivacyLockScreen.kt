package com.tourisain.weijian.presentation.privacy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PrivacyLockScreen(
    onUnlock: () -> Unit,
    requirePassword: Boolean = false,
    viewModel: PrivacyViewModel = hiltViewModel()
) {
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
    LaunchedEffect(requirePassword) {
        if (requirePassword) viewModel.startRequiredUnlock()
    }
    LaunchedEffect(isUnlocked) { if (isUnlocked) onUnlock() }
    PrivacyKeypad(viewModel = viewModel)
}
