package com.tourisain.weijian.presentation.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.common.AppleAlertDialog
import com.tourisain.weijian.presentation.common.popBackStackStable
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import com.tourisain.weijian.util.ActivationCodeManager
import com.tourisain.weijian.util.PremiumManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    @Suppress("UNUSED_PARAMETER") premiumManager: PremiumManager,
    @Suppress("UNUSED_PARAMETER") activationCodeManager: ActivationCodeManager
) {
    val isUserPro by viewModel.isUserPro.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val activationIdentity by viewModel.activationIdentity.collectAsStateWithLifecycle()
    val isActivating by viewModel.isActivating.collectAsStateWithLifecycle()
    val activationFeedback by viewModel.activationFeedback.collectAsStateWithLifecycle()
    val activationCode by viewModel.activationCode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var showPurchaseDialog by remember { mutableStateOf(false) }
    val activationSucceeded = activationFeedback?.success == true
    val displayedIsUserPro = isUserPro || activationSucceeded
    val displayedLevel = if (displayedIsUserPro) PremiumManager.MEMBERSHIP_LEVEL_LIFETIME else 0
    var showLifetimeSuccessAnimation by remember { mutableStateOf(false) }
    var lastLifetimeAnimationKey by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(toastMessage) {
        val message = toastMessage
        if (!message.isNullOrBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(activationFeedback) {
        val feedback = activationFeedback
        if (feedback?.success == true && feedback.level >= 2 && feedback.expiresAt == null) {
            val animationKey = "${feedback.message}|${feedback.level}|${feedback.expiresAt}"
            if (lastLifetimeAnimationKey == animationKey) return@LaunchedEffect
            lastLifetimeAnimationKey = animationKey
            showLifetimeSuccessAnimation = false
            delay(80)
            showLifetimeSuccessAnimation = true
            delay(3400)
            showLifetimeSuccessAnimation = false
        }
    }

    Scaffold(
        containerColor = AppleNotesStyle.Background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStackStable(Screen.Profile.route) }) {
                        Icon(
                            Lucide.ChevronLeft,
                            contentDescription = stringResource(R.string.back),
                            tint = AppleNotesStyle.Accent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppleNotesStyle.Background)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppleNotesStyle.Background)
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppleNotesStyle.Background),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.membership_center),
                            color = AppleNotesStyle.PrimaryText,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.membership_offline_activation_subtitle),
                            color = AppleNotesStyle.SecondaryText
                        )
                    }
                }
                item {
                    MembershipStatusCard(
                        isUserPro = displayedIsUserPro,
                        level = displayedLevel
                    )
                }
                if (!displayedIsUserPro) {
                    item {
                        PurchaseMembershipCard(onClick = { showPurchaseDialog = true })
                    }
                }
                item {
                    ActivationCard(
                        state = activationIdentity,
                        activationCode = activationCode,
                        isActivating = isActivating,
                        feedback = activationFeedback,
                        onUsernameChange = viewModel::updateActivationUsername,
                        onEmailChange = viewModel::updateActivationEmail,
                        onActivationCodeChange = {
                            viewModel.updateActivationCode(normalizeActivationCodeInput(it))
                        },
                        onGenerateDeviceCode = viewModel::generateActivationDeviceCode,
                        onCopyDeviceCode = {
                            if (activationIdentity.deviceCode.isNotBlank()) {
                                clipboard.setText(AnnotatedString(activationIdentity.deviceCode))
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.activation_device_code_copied),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onActivate = {
                            viewModel.upgradeToPro(
                                activationCode = activationCode,
                                username = activationIdentity.username,
                                email = activationIdentity.email
                            )
                        }
                    )
                }
                item {
                    BenefitsCard()
                }
                item {
                    Text(
                        text = stringResource(
                            R.string.activation_security_footer,
                            activationIdentity.localFingerprint.ifBlank { viewModel.getDeviceActivationFingerprint() }
                        ),
                        color = AppleNotesStyle.TertiaryText,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            LifetimeActivationSuccessOverlay(
                visible = showLifetimeSuccessAnimation,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }

    if (showPurchaseDialog) {
        AppleAlertDialog(
            onDismissRequest = { showPurchaseDialog = false },
            title = { Text(stringResource(R.string.membership_purchase_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = MEMBERSHIP_PURCHASE_DIALOG_TEXT_MAX_HEIGHT_DP.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        stringResource(
                            R.string.membership_purchase_message,
                            "tourisain@163.com",
                            "D-grllq",
                            "3596202385"
                        ),
                        color = AppleNotesStyle.SecondaryText
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPurchaseDialog = false
                    openMembershipPurchaseEmail(context)
                }) {
                    Text(stringResource(R.string.membership_purchase_email_action), color = AppleNotesStyle.Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

internal const val MEMBERSHIP_PURCHASE_DIALOG_TEXT_MAX_HEIGHT_DP = 220

@Composable
private fun LifetimeActivationSuccessOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val pulse by rememberInfiniteTransition(label = "lifetime-success-pulse").animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 860),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lifetime-success-scale"
    )
    val halo by rememberInfiniteTransition(label = "lifetime-success-halo").animateFloat(
        initialValue = 0.82f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1120),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lifetime-success-halo-scale"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)) +
            slideInVertically(animationSpec = tween(320), initialOffsetY = { it / 5 }) +
            scaleIn(animationSpec = tween(320), initialScale = 0.9f),
        exit = fadeOut(animationSpec = tween(240)) +
            slideOutVertically(animationSpec = tween(240), targetOffsetY = { -it / 5 }) +
            scaleOut(animationSpec = tween(240), targetScale = 0.96f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.06f))
                .padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = AppleNotesStyle.Surface,
                shape = AppleNotesStyle.GroupShape,
                tonalElevation = 0.dp,
                shadowElevation = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(modifier = Modifier.size(132.dp), contentAlignment = Alignment.Center) {
                        Surface(
                            color = AppleNotesStyle.AccentSoft.copy(alpha = 0.42f),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(128.dp)
                                .scale(halo)
                                .alpha(0.76f)
                        ) {}
                        Surface(
                            color = AppleNotesStyle.AccentSoft,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(94.dp)
                                .scale(pulse)
                        ) {}
                        Surface(color = AppleNotesStyle.Surface, shape = CircleShape, shadowElevation = 8.dp) {
                            Box(modifier = Modifier.size(74.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    Lucide.Star,
                                    contentDescription = null,
                                    tint = AppleNotesStyle.Accent,
                                    modifier = Modifier.size(42.dp)
                                )
                                Surface(
                                    color = AppleNotesStyle.Accent,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Lucide.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        stringResource(R.string.membership_lifetime_activation_celebration_title),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.membership_lifetime_activation_celebration_subtitle),
                        color = AppleNotesStyle.SecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LifetimeCelebrationPill(
                            icon = Lucide.Sparkles,
                            text = stringResource(R.string.membership_lifetime_active)
                        )
                        LifetimeCelebrationPill(
                            icon = Lucide.Shield,
                            text = stringResource(R.string.membership_benefits)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LifetimeCelebrationPill(icon: ImageVector, text: String) {
    Surface(color = AppleNotesStyle.AccentSoft, shape = CircleShape) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(15.dp))
            Text(text, color = AppleNotesStyle.PrimaryText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MembershipStatusCard(isUserPro: Boolean, level: Int) {
    Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = if (isUserPro) AppleNotesStyle.AccentSoft else AppleNotesStyle.SearchSurface, shape = CircleShape) {
                Box(modifier = Modifier.size(58.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        if (isUserPro) Lucide.Star else Lucide.User,
                        contentDescription = null,
                        tint = if (isUserPro) AppleNotesStyle.Accent else AppleNotesStyle.SecondaryText,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.membership_current_plan),
                    color = AppleNotesStyle.SecondaryText,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    membershipName(level, isUserPro),
                    color = AppleNotesStyle.PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(expireText(isUserPro), color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PurchaseMembershipCard(onClick: () -> Unit) {
    Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = AppleNotesStyle.AccentSoft, shape = CircleShape) {
                    Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                        Icon(Lucide.Star, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.membership_purchase_button), color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.membership_purchase_subtitle), color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
                }
            }
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = AppleNotesStyle.ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = AppleNotesStyle.Accent, contentColor = Color.Black)
            ) {
                Text(stringResource(R.string.membership_purchase_button), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ActivationCard(
    state: ActivationIdentityUiState,
    activationCode: String,
    isActivating: Boolean,
    feedback: ActivationFeedbackUiState?,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onActivationCodeChange: (String) -> Unit,
    onGenerateDeviceCode: () -> Unit,
    onCopyDeviceCode: () -> Unit,
    onActivate: () -> Unit
) {
    Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Lucide.Key, contentDescription = null, tint = AppleNotesStyle.Accent, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.activation_title), color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
                    Text(
                        stringResource(R.string.activation_description),
                        color = AppleNotesStyle.SecondaryText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            ActivationTextField(
                value = state.username,
                onValueChange = onUsernameChange,
                label = stringResource(R.string.activation_name_label),
                icon = Lucide.User
            )
            ActivationTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = stringResource(R.string.activation_email_label),
                icon = Lucide.Mail,
                keyboardType = KeyboardType.Email
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppleNotesStyle.SearchSurface, AppleNotesStyle.SearchShape)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(R.string.activation_device_code_label),
                    color = AppleNotesStyle.SecondaryText,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    state.deviceCode.ifBlank { stringResource(R.string.activation_device_code_empty) },
                    color = if (state.deviceCode.isBlank()) AppleNotesStyle.TertiaryText else AppleNotesStyle.PrimaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onGenerateDeviceCode,
                        modifier = Modifier.weight(1f),
                        shape = AppleNotesStyle.ButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = AppleNotesStyle.Accent, contentColor = Color.Black)
                    ) {
                        Text(stringResource(R.string.activation_generate_device_code), fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = onCopyDeviceCode, enabled = state.deviceCode.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Lucide.Copy, contentDescription = null, modifier = Modifier.size(16.dp), tint = AppleNotesStyle.Accent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.copy), color = AppleNotesStyle.Accent)
                        }
                    }
                }
            }

            ActivationTextField(
                value = activationCode,
                onValueChange = onActivationCodeChange,
                label = stringResource(R.string.activation_code_label),
                icon = Lucide.Lock,
                singleLine = false,
                minLines = 3,
                maxLines = 6
            )
            Text(
                stringResource(R.string.activation_generator_note),
                color = AppleNotesStyle.TertiaryText,
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = onActivate,
                enabled = activationCode.isNotBlank() && !isActivating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = AppleNotesStyle.ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = AppleNotesStyle.Accent, contentColor = Color.Black)
            ) {
                Text(
                    stringResource(if (isActivating) R.string.activation_activating else R.string.activation_activate),
                    fontWeight = FontWeight.SemiBold
                )
            }
            feedback?.message?.takeIf { it.isNotBlank() }?.let { message ->
                Surface(
                    color = if (feedback.success) AppleNotesStyle.AccentSoft else AppleNotesStyle.SearchSurface,
                    shape = AppleNotesStyle.SearchShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message,
                        color = if (feedback.success) AppleNotesStyle.Accent else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 4
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = AppleNotesStyle.SecondaryText) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = AppleNotesStyle.SearchShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppleNotesStyle.Accent,
            unfocusedBorderColor = AppleNotesStyle.Separator,
            cursorColor = AppleNotesStyle.Accent,
            focusedLabelColor = AppleNotesStyle.Accent
        )
    )
}

@Composable
private fun BenefitsCard() {
    Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.membership_benefits), color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
            BenefitRow(
                icon = Lucide.FileText,
                title = stringResource(R.string.membership_benefit_unlimited_notes_title),
                subtitle = stringResource(R.string.membership_benefit_unlimited_notes_subtitle)
            )
            BenefitRow(
                icon = Lucide.PiggyBank,
                title = stringResource(R.string.membership_benefit_unlimited_accounts_title),
                subtitle = stringResource(R.string.membership_benefit_unlimited_accounts_subtitle)
            )
            BenefitRow(
                icon = Lucide.Cloud,
                title = stringResource(R.string.membership_benefit_backup_title),
                subtitle = stringResource(R.string.membership_benefit_backup_subtitle)
            )
            BenefitRow(
                icon = Lucide.ChartBar,
                title = stringResource(R.string.membership_benefit_stats_title),
                subtitle = stringResource(R.string.membership_benefit_stats_subtitle)
            )
            BenefitRow(
                icon = Lucide.Palette,
                title = stringResource(R.string.membership_benefit_style_title),
                subtitle = stringResource(R.string.membership_benefit_style_subtitle)
            )
            BenefitRow(
                icon = Lucide.Shield,
                title = stringResource(R.string.membership_benefit_local_title),
                subtitle = stringResource(R.string.membership_benefit_local_subtitle)
            )
            BenefitRow(
                icon = Lucide.Shield,
                title = stringResource(R.string.membership_benefit_no_tracking_title),
                subtitle = stringResource(R.string.membership_benefit_no_tracking_subtitle)
            )
        }
    }
}

@Composable
private fun BenefitRow(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = AppleNotesStyle.AccentSoft, shape = CircleShape) {
            Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color(0xFF9A6A00), modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun membershipName(level: Int, isUserPro: Boolean): String {
    if (!isUserPro) return stringResource(R.string.standard_user)
    return when (level) {
        2 -> stringResource(R.string.lifetime_member)
        else -> stringResource(R.string.premium_member)
    }
}

@Composable
private fun expireText(isUserPro: Boolean): String {
    if (!isUserPro) return stringResource(R.string.membership_not_active)
    return stringResource(R.string.membership_lifetime_active)
}

private fun openMembershipPurchaseEmail(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:tourisain@163.com")
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.membership_purchase_email_subject))
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.membership_purchase_email_body))
    }
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, context.getString(R.string.membership_purchase_no_email_app), Toast.LENGTH_LONG).show()
    }
}
