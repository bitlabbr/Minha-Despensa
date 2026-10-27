/*
 *   Copyright (c) 2026 Willian Santos
 *
 *   This work is licensed under the Creative Commons
 *   Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 *
 *   You are free to:
 *     - Share  — copy and redistribute the material in any medium or format
 *     - Adapt  — remix, transform, and build upon the material
 *
 *   Under the following terms:
 *     - Attribution    — You must give appropriate credit, provide a link to
 *                        the license, and indicate if changes were made.
 *     - NonCommercial  — You may not use the material for commercial purposes.
 *
 *   Owner rights:
 *     - Willian Santos retains all commercial rights.
 *    - The copyright holder may use, sell, sublicense, or relicense this
 *       work under different terms at any time.
 *
 *   Full license: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package com.bitlabbr.minhadespensa.uisystem.features.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bitlabbr.minhadespensa.core.domain.model.HouseholdRole
import com.bitlabbr.minhadespensa.core.domain.model.SyncState
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaPrimaryButton
import com.bitlabbr.minhadespensa.uisystem.components.core.button.MinhaDespensaSecondaryButton
import com.bitlabbr.minhadespensa.uisystem.features.account.widgets.AppUpdateSection
import com.bitlabbr.minhadespensa.uisystem.features.account.widgets.BugReportDialog
import com.bitlabbr.minhadespensa.uisystem.features.account.widgets.SupportFeedbackSection
import com.bitlabbr.minhadespensa.uisystem.components.core.card.ItemContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.card.PrimaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.card.SecondaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.dialog.MinhaDespensaDialog
import com.bitlabbr.minhadespensa.uisystem.components.core.feedback.rememberHapticFeedbackManager
import com.bitlabbr.minhadespensa.uisystem.components.core.text.MinhaDespensaText
import com.bitlabbr.minhadespensa.uisystem.components.core.topbar.MinhaDespensaTopBar
import com.bitlabbr.minhadespensa.uisystem.components.domain.catalog.ProductTextField
import com.bitlabbr.minhadespensa.uisystem.features.account.model.AuthTab
import com.bitlabbr.minhadespensa.uisystem.features.account.model.HouseholdAccountUiState
import com.bitlabbr.minhadespensa.uisystem.features.account.widgets.CreateHouseholdDialog
import com.bitlabbr.minhadespensa.uisystem.features.account.widgets.HouseholdMemberCard
import com.bitlabbr.minhadespensa.uisystem.features.account.widgets.JoinHouseholdDialog
import com.bitlabbr.minhadespensa.uisystem.manager.AppNotificationManager
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.account_auth_banner_desc
import minhadespensa.uisystem.generated.resources.account_auth_banner_title
import minhadespensa.uisystem.generated.resources.account_button_login
import minhadespensa.uisystem.generated.resources.account_button_logout
import minhadespensa.uisystem.generated.resources.account_button_register
import minhadespensa.uisystem.generated.resources.account_input_email_label
import minhadespensa.uisystem.generated.resources.account_input_email_placeholder
import minhadespensa.uisystem.generated.resources.account_input_name_label
import minhadespensa.uisystem.generated.resources.account_input_name_placeholder
import minhadespensa.uisystem.generated.resources.account_input_password_label
import minhadespensa.uisystem.generated.resources.account_input_password_placeholder
import minhadespensa.uisystem.generated.resources.account_logout_dialog_cancel
import minhadespensa.uisystem.generated.resources.account_logout_dialog_confirm
import minhadespensa.uisystem.generated.resources.account_logout_dialog_desc
import minhadespensa.uisystem.generated.resources.account_logout_dialog_title
import minhadespensa.uisystem.generated.resources.account_merge_dialog_desc
import minhadespensa.uisystem.generated.resources.account_merge_dialog_title
import minhadespensa.uisystem.generated.resources.account_merge_option_discard
import minhadespensa.uisystem.generated.resources.account_merge_option_merge
import minhadespensa.uisystem.generated.resources.account_screen_title
import minhadespensa.uisystem.generated.resources.account_tab_login
import minhadespensa.uisystem.generated.resources.account_tab_register
import minhadespensa.uisystem.generated.resources.household_action_create
import minhadespensa.uisystem.generated.resources.household_action_join
import minhadespensa.uisystem.generated.resources.household_action_switch
import minhadespensa.uisystem.generated.resources.household_active_title
import minhadespensa.uisystem.generated.resources.household_invite_code_copied
import minhadespensa.uisystem.generated.resources.household_invite_code_desc
import minhadespensa.uisystem.generated.resources.household_invite_code_label
import minhadespensa.uisystem.generated.resources.household_members_title
import minhadespensa.uisystem.generated.resources.household_other_households_title
import minhadespensa.uisystem.generated.resources.household_role_member
import minhadespensa.uisystem.generated.resources.household_role_owner
import minhadespensa.uisystem.generated.resources.register_product_form_back_button_desc
import minhadespensa.uisystem.generated.resources.sync_button_sync_now
import minhadespensa.uisystem.generated.resources.sync_session_expired_banner_desc
import minhadespensa.uisystem.generated.resources.sync_session_expired_banner_title
import minhadespensa.uisystem.generated.resources.sync_status_error
import minhadespensa.uisystem.generated.resources.sync_status_idle
import minhadespensa.uisystem.generated.resources.sync_status_offline
import minhadespensa.uisystem.generated.resources.sync_status_session_expired
import minhadespensa.uisystem.generated.resources.sync_status_success
import minhadespensa.uisystem.generated.resources.sync_status_syncing
import minhadespensa.uisystem.generated.resources.sync_topbar_sync_desc
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HouseholdAccountScreen(
    onNavigateBack: () -> Unit,
    bottomPadding: Dp = 0.dp,
    viewModel: HouseholdAccountViewModel = koinViewModel(),
    notificationManager: AppNotificationManager = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography
    val clipboardManager = LocalClipboardManager.current
    val hapticFeedbackManager = rememberHapticFeedbackManager()

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            MinhaDespensaTopBar(
                backgroundColor = Color.Transparent,
                leftContent = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(Res.string.register_product_form_back_button_desc),
                            tint = colors.onPrimaryContainer,
                        )
                    }
                },
                centerContent = {
                    MinhaDespensaText(
                        text = stringResource(Res.string.account_screen_title),
                        fontStyle = typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onPrimaryContainer,
                    )
                },
                rightContent = {
                    if (uiState.isAuthenticated) {
                        IconButton(
                            onClick = { viewModel.triggerSync() },
                            enabled = !uiState.isSyncing,
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = colors.primary,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Sync,
                                    contentDescription = stringResource(Res.string.sync_topbar_sync_desc),
                                    tint = colors.onPrimaryContainer,
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = bottomPadding)
                .padding(horizontal = dimens.paddingSmall)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
        ) {
            if (!uiState.isAuthenticated) {
                UnauthenticatedContent(
                    uiState = uiState,
                    onTabSelected = viewModel::onTabSelected,
                    onLoginEmailChange = viewModel::onLoginEmailChange,
                    onLoginPasswordChange = viewModel::onLoginPasswordChange,
                    onRegisterNameChange = viewModel::onRegisterNameChange,
                    onRegisterEmailChange = viewModel::onRegisterEmailChange,
                    onRegisterPasswordChange = viewModel::onRegisterPasswordChange,
                    onLogin = viewModel::login,
                    onRegister = viewModel::register,
                )
            } else {
                AuthenticatedContent(
                    uiState = uiState,
                    onSyncClick = viewModel::triggerSync,
                    onCopyInviteCode = { code ->
                        clipboardManager.setText(AnnotatedString(code))
                        hapticFeedbackManager.performSuccessFeedback()
                        notificationManager.showSuccess(UiText.Resource(Res.string.household_invite_code_copied))
                    },
                    onOpenJoinDialog = viewModel::openJoinDialog,
                    onOpenCreateDialog = viewModel::openCreateDialog,
                    onOpenLogoutDialog = viewModel::openLogoutDialog,
                    onSwitchHousehold = viewModel::switchActiveHousehold,
                )
            }

            AppUpdateSection(
                uiState = uiState,
                onCheckUpdate = viewModel::checkForUpdates,
                onDownloadAndInstall = viewModel::downloadAndInstallUpdate,
            )

            SupportFeedbackSection(
                uiState = uiState,
                onOpenReportDialog = viewModel::openBugReportDialog,
                onExportLogs = viewModel::shareDiagnosticsLog,
                onDismissPendingCrash = viewModel::dismissPendingCrashBanner,
            )

            Spacer(Modifier.height(dimens.paddingLarge))
        }
    }

    if (uiState.isJoinDialogOpen) {
        JoinHouseholdDialog(
            inviteCode = uiState.joinInviteCode,
            onInviteCodeChange = viewModel::onJoinInviteCodeChange,
            onConfirm = viewModel::joinHousehold,
            onDismiss = viewModel::dismissJoinDialog,
            isLoading = uiState.isLoading,
        )
    }

    if (uiState.isCreateDialogOpen) {
        CreateHouseholdDialog(
            name = uiState.createHouseholdName,
            onNameChange = viewModel::onCreateHouseholdNameChange,
            onConfirm = viewModel::createHousehold,
            onDismiss = viewModel::dismissCreateDialog,
            isLoading = uiState.isLoading,
        )
    }

    if (uiState.isLogoutConfirmDialogOpen) {
        MinhaDespensaDialog(
            onDismissRequest = viewModel::dismissLogoutDialog,
            title = stringResource(Res.string.account_logout_dialog_title),
            description = stringResource(Res.string.account_logout_dialog_desc),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
            ) {
                MinhaDespensaSecondaryButton(
                    text = stringResource(Res.string.account_logout_dialog_cancel),
                    onClick = viewModel::dismissLogoutDialog,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.weight(1f),
                )

                MinhaDespensaPrimaryButton(
                    text = stringResource(Res.string.account_logout_dialog_confirm),
                    onClick = viewModel::logout,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (uiState.isMergeDecisionDialogOpen) {
        MinhaDespensaDialog(
            onDismissRequest = viewModel::dismissMergeDecisionDialog,
            title = stringResource(Res.string.account_merge_dialog_title),
            description = stringResource(Res.string.account_merge_dialog_desc),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
            ) {
                MinhaDespensaPrimaryButton(
                    text = stringResource(Res.string.account_merge_option_merge),
                    onClick = viewModel::onConfirmMergeData,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )

                MinhaDespensaSecondaryButton(
                    text = stringResource(Res.string.account_merge_option_discard),
                    onClick = viewModel::onConfirmDiscardLocalData,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (uiState.isBugReportDialogOpen) {
        BugReportDialog(
            uiState = uiState,
            onCategoryChange = viewModel::onBugReportCategoryChange,
            onTitleChange = viewModel::onBugReportTitleChange,
            onDescriptionChange = viewModel::onBugReportDescriptionChange,
            onContactChange = viewModel::onBugReportContactChange,
            onIncludeLogsChange = viewModel::onBugReportIncludeLogsChange,
            onSubmit = viewModel::submitBugReport,
            onDismiss = viewModel::dismissBugReportDialog,
        )
    }
}

@Composable
private fun UnauthenticatedContent(
    uiState: HouseholdAccountUiState,
    onTabSelected: (AuthTab) -> Unit,
    onLoginEmailChange: (String) -> Unit,
    onLoginPasswordChange: (String) -> Unit,
    onRegisterNameChange: (String) -> Unit,
    onRegisterEmailChange: (String) -> Unit,
    onRegisterPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
) {
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography

    ItemContainerGlassCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudSync,
                    contentDescription = null,
                    tint = colors.primary,
                )
                MinhaDespensaText(
                    text = stringResource(Res.string.account_auth_banner_title),
                    fontStyle = typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer,
                )
            }

            MinhaDespensaText(
                text = stringResource(Res.string.account_auth_banner_desc),
                fontStyle = typography.bodySmall,
                color = colors.onSecondaryContainer.copy(alpha = 0.8f),
            )
        }
    }

    SecondaryContainerGlassCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = colors.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(4.dp),
            ) {
                val isLogin = uiState.selectedTab == AuthTab.LOGIN
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isLogin) colors.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .clickable { onTabSelected(AuthTab.LOGIN) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    MinhaDespensaText(
                        text = stringResource(Res.string.account_tab_login),
                        fontStyle = typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLogin) Color.White else colors.onPrimaryContainer,
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (!isLogin) colors.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .clickable { onTabSelected(AuthTab.REGISTER) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    MinhaDespensaText(
                        text = stringResource(Res.string.account_tab_register),
                        fontStyle = typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (!isLogin) Color.White else colors.onPrimaryContainer,
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.selectedTab == AuthTab.LOGIN,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    ProductTextField(
                        value = uiState.loginEmail,
                        onValueChange = onLoginEmailChange,
                        label = stringResource(Res.string.account_input_email_label),
                        placeholder = stringResource(Res.string.account_input_email_placeholder),
                        keyboardType = KeyboardType.Email,
                        singleLine = true,
                        isRequired = true,
                    )

                    ProductTextField(
                        value = uiState.loginPassword,
                        onValueChange = onLoginPasswordChange,
                        label = stringResource(Res.string.account_input_password_label),
                        placeholder = stringResource(Res.string.account_input_password_placeholder),
                        keyboardType = KeyboardType.Password,
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isRequired = true,
                    )

                    Spacer(Modifier.height(dimens.paddingSmall))

                    MinhaDespensaPrimaryButton(
                        text = stringResource(Res.string.account_button_login),
                        onClick = onLogin,
                        enabled = uiState.loginEmail.isNotBlank() && uiState.loginPassword.isNotBlank() && !uiState.isLoading,
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.selectedTab == AuthTab.REGISTER,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
                ) {
                    ProductTextField(
                        value = uiState.registerName,
                        onValueChange = onRegisterNameChange,
                        label = stringResource(Res.string.account_input_name_label),
                        placeholder = stringResource(Res.string.account_input_name_placeholder),
                        maxCharacters = CoreConstants.Auth.NAME_MAX_LENGTH,
                        singleLine = true,
                        isRequired = true,
                    )

                    ProductTextField(
                        value = uiState.registerEmail,
                        onValueChange = onRegisterEmailChange,
                        label = stringResource(Res.string.account_input_email_label),
                        placeholder = stringResource(Res.string.account_input_email_placeholder),
                        keyboardType = KeyboardType.Email,
                        singleLine = true,
                        isRequired = true,
                    )

                    ProductTextField(
                        value = uiState.registerPassword,
                        onValueChange = onRegisterPasswordChange,
                        label = stringResource(Res.string.account_input_password_label),
                        placeholder = stringResource(Res.string.account_input_password_placeholder),
                        keyboardType = KeyboardType.Password,
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isRequired = true,
                    )

                    Spacer(Modifier.height(dimens.paddingSmall))

                    MinhaDespensaPrimaryButton(
                        text = stringResource(Res.string.account_button_register),
                        onClick = onRegister,
                        enabled = uiState.registerName.isNotBlank() && uiState.registerEmail.isNotBlank() && uiState.registerPassword.length >= 8 && !uiState.isLoading,
                        isLoading = uiState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedContent(
    uiState: HouseholdAccountUiState,
    onSyncClick: () -> Unit,
    onCopyInviteCode: (String) -> Unit,
    onOpenJoinDialog: () -> Unit,
    onOpenCreateDialog: () -> Unit,
    onOpenLogoutDialog: () -> Unit,
    onSwitchHousehold: (String) -> Unit,
) {
    val colors = getAppColors()
    val dimens = MinhaDespensaTheme.dimens
    val typography = MinhaDespensaTheme.typography
    val user = uiState.session?.user

    SecondaryContainerGlassCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.paddingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                MinhaDespensaText(
                    text = user?.name ?: "",
                    fontStyle = typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer,
                )
                MinhaDespensaText(
                    text = user?.email ?: "",
                    fontStyle = typography.bodySmall,
                    color = colors.onSecondaryContainer.copy(alpha = 0.7f),
                )
            }

            IconButton(onClick = onOpenLogoutDialog) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = stringResource(Res.string.account_button_logout),
                    tint = colors.onSecondaryContainer,
                )
            }
        }
    }

    if (uiState.syncState == SyncState.SESSION_EXPIRED) {
        ItemContainerGlassCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.paddingSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = colors.error,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    MinhaDespensaText(
                        text = stringResource(Res.string.sync_session_expired_banner_title),
                        fontStyle = typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.error,
                    )
                    MinhaDespensaText(
                        text = stringResource(Res.string.sync_session_expired_banner_desc),
                        fontStyle = typography.bodySmall,
                        color = colors.onSecondaryContainer,
                    )
                }
            }
        }
    }

    PrimaryContainerGlassCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = null,
                        tint = colors.primary,
                    )
                    MinhaDespensaText(
                        text = stringResource(Res.string.household_active_title),
                        fontStyle = typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = colors.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }

                val isOwner = uiState.isOwnerOfActiveHousehold
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isOwner) colors.primary.copy(alpha = 0.2f) else colors.onSecondaryContainer.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    MinhaDespensaText(
                        text = stringResource(
                            if (isOwner) Res.string.household_role_owner else Res.string.household_role_member
                        ),
                        fontStyle = typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOwner) colors.primary else colors.onSecondaryContainer,
                    )
                }
            }

            MinhaDespensaText(
                text = uiState.activeHousehold?.name ?: "",
                fontStyle = typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onPrimaryContainer,
            )

            val inviteCode = uiState.activeHousehold?.inviteCode ?: ""
            if (inviteCode.isNotBlank()) {
                ItemContainerGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.paddingSmall),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        MinhaDespensaText(
                            text = stringResource(Res.string.household_invite_code_label),
                            fontStyle = typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSecondaryContainer,
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = colors.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .border(
                                    width = 1.dp,
                                    color = colors.primary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .clickable { onCopyInviteCode(inviteCode) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            MinhaDespensaText(
                                text = inviteCode,
                                fontStyle = typography.bodyLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = colors.primary,
                            )

                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }

                        MinhaDespensaText(
                            text = stringResource(Res.string.household_invite_code_desc),
                            fontStyle = typography.bodySmall,
                            color = colors.onSecondaryContainer.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val (syncIcon, syncLabel, syncColor) = when (uiState.syncState) {
                        SyncState.SYNCING -> Triple(Icons.Rounded.CloudSync, stringResource(Res.string.sync_status_syncing), colors.primary)
                        SyncState.SUCCESS -> Triple(Icons.Rounded.CloudDone, stringResource(Res.string.sync_status_success), colors.primary)
                        SyncState.ERROR -> Triple(Icons.Rounded.CloudOff, stringResource(Res.string.sync_status_error), colors.error)
                        SyncState.OFFLINE_UNAUTHENTICATED -> Triple(Icons.Rounded.CloudOff, stringResource(Res.string.sync_status_offline), colors.onSecondaryContainer)
                        SyncState.SESSION_EXPIRED -> Triple(Icons.Rounded.CloudOff, stringResource(Res.string.sync_status_session_expired), colors.error)
                        SyncState.IDLE -> Triple(Icons.Rounded.CloudDone, stringResource(Res.string.sync_status_idle), colors.onSecondaryContainer)
                    }

                    Icon(
                        imageVector = syncIcon,
                        contentDescription = null,
                        tint = syncColor,
                        modifier = Modifier.size(18.dp),
                    )

                    MinhaDespensaText(
                        text = syncLabel,
                        fontStyle = typography.bodySmall,
                        color = syncColor,
                        fontWeight = FontWeight.Medium,
                    )
                }

                MinhaDespensaSecondaryButton(
                    text = stringResource(Res.string.sync_button_sync_now),
                    onClick = onSyncClick,
                    enabled = !uiState.isSyncing,
                )
            }

            val members = uiState.activeHouseholdDetail?.members ?: emptyList()
            if (members.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Group,
                        contentDescription = null,
                        tint = colors.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                    MinhaDespensaText(
                        text = "${stringResource(Res.string.household_members_title)} (${members.size})",
                        fontStyle = typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onPrimaryContainer,
                    )
                }

                members.forEach { member ->
                    HouseholdMemberCard(member = member)
                }
            }
        }
    }

    val otherHouseholds = uiState.userHouseholds.filter { it.id != uiState.activeHousehold?.id }
    if (otherHouseholds.isNotEmpty()) {
        SecondaryContainerGlassCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
            ) {
                MinhaDespensaText(
                    text = stringResource(Res.string.household_other_households_title),
                    fontStyle = typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onPrimaryContainer,
                )

                otherHouseholds.forEach { household ->
                    ItemContainerGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimens.paddingSmall),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                MinhaDespensaText(
                                    text = household.name,
                                    fontStyle = typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onPrimaryContainer,
                                )
                                MinhaDespensaText(
                                    text = "${household.members.size} membros",
                                    fontStyle = typography.bodySmall,
                                    color = colors.onSecondaryContainer.copy(alpha = 0.7f),
                                )
                            }

                            MinhaDespensaSecondaryButton(
                                text = stringResource(Res.string.household_action_switch),
                                onClick = { onSwitchHousehold(household.id) },
                                enabled = !uiState.isSwitchingHousehold,
                            )
                        }
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
    ) {
        MinhaDespensaSecondaryButton(
            text = stringResource(Res.string.household_action_join),
            onClick = onOpenJoinDialog,
            modifier = Modifier.weight(1f),
        )

        MinhaDespensaPrimaryButton(
            text = stringResource(Res.string.household_action_create),
            onClick = onOpenCreateDialog,
            modifier = Modifier.weight(1f),
        )
    }
}
