package com.campusassistant.android.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.campusassistant.android.ui.common.CampusCardStyleProvider
import com.campusassistant.android.ui.emptyclassroom.EmptyClassroomUiState
import com.campusassistant.android.ui.grades.GradesUiState
import com.campusassistant.android.ui.login.LoginScreen
import com.campusassistant.android.ui.navigation.MainShell
import com.campusassistant.android.ui.notice.NoticeOverlay
import com.campusassistant.android.ui.notice.NoticeUiState
import com.campusassistant.android.ui.profile.ProfileUiState
import com.campusassistant.android.ui.schedule.ScheduleUiState
import com.campusassistant.android.ui.splash.SplashScreen
import com.campusassistant.android.ui.text.AppTextProvider
import kotlinx.coroutines.delay

@Composable
fun AppRoot(
    appState: AppUiState,
    noticeState: NoticeUiState,
    profileState: ProfileUiState,
    scheduleState: ScheduleUiState,
    gradesState: GradesUiState,
    emptyClassroomState: EmptyClassroomUiState,
    authActions: AuthActions,
    noticeActions: NoticeActions,
    profileActions: ProfileActions,
    scheduleActions: ScheduleActions,
    gradesActions: GradesActions,
    emptyClassroomActions: EmptyClassroomActions
) {
    var splashVisible by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1700)
        splashVisible = false
        noticeActions.onRefresh()
    }

    LaunchedEffect(appState.authState, appState.sessionVersion) {
        when (appState.authState) {
            AuthState.LoggedIn -> {
                profileActions.onRefresh()
                scheduleActions.onRefresh()
            }
            AuthState.LoggedOut -> authActions.onLoggedOut()
            AuthState.Checking -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GlobalWallpaperLayer(profileState = profileState)
        AppTextProvider {
            CampusCardStyleProvider(
                opacity = profileState.personalizationDraft.cardOpacity,
                blur = profileState.personalizationDraft.cardBlur
            ) {
                AnimatedContent(
                    targetState = appState.authState to appState.loginPreviewVisible,
                    transitionSpec = {
                        ContentTransform(
                            targetContentEnter = fadeIn(animationSpec = tween(220)),
                            initialContentExit = fadeOut(animationSpec = tween(180))
                        )
                    },
                    label = "auth-transition"
                ) { (authState, loginPreviewVisible) ->
                    when {
                        authState == AuthState.Checking -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }

                        authState == AuthState.LoggedOut -> {
                            LoginScreen(
                                loading = appState.loginLoading,
                                errorMessage = appState.loginError,
                                serverSettingsDraft = profileState.serverSettingsDraft,
                                savingServerConfig = profileState.savingServerConfig,
                                onServerSettingsToggle = profileActions.onServerSettingsToggle,
                                onServerHostChange = profileActions.onServerHostChange,
                                onServerPortChange = profileActions.onServerPortChange,
                                onSaveServerSettings = profileActions.onSaveServerSettings,
                                showBackToApp = appState.tokenExists && loginPreviewVisible,
                                onBackToApp = authActions.onHideLoginPreview,
                                onLogin = authActions.onLogin
                            )
                        }

                        loginPreviewVisible -> {
                            LoginScreen(
                                loading = appState.loginLoading,
                                errorMessage = appState.loginError,
                                serverSettingsDraft = profileState.serverSettingsDraft,
                                savingServerConfig = profileState.savingServerConfig,
                                onServerSettingsToggle = profileActions.onServerSettingsToggle,
                                onServerHostChange = profileActions.onServerHostChange,
                                onServerPortChange = profileActions.onServerPortChange,
                                onSaveServerSettings = profileActions.onSaveServerSettings,
                                showBackToApp = true,
                                onBackToApp = authActions.onHideLoginPreview,
                                onLogin = authActions.onLogin
                            )
                        }

                        else -> {
                            MainShell(
                                tokenExists = appState.tokenExists,
                                profileState = profileState,
                                scheduleState = scheduleState,
                                gradesState = gradesState,
                                emptyClassroomState = emptyClassroomState,
                                onShowLoginPreview = authActions.onShowLoginPreview,
                                onLogout = authActions.onLogout,
                                profileActions = profileActions,
                                scheduleActions = scheduleActions,
                                gradesActions = gradesActions,
                                emptyClassroomActions = emptyClassroomActions
                            )
                        }
                    }
                }

                NoticeOverlay(
                    state = noticeState,
                    onOpenLatestNotice = noticeActions.onOpenLatest,
                    onOpenNoticeHistory = noticeActions.onOpenHistory,
                    onOpenHistoryNotice = noticeActions.onOpenHistoryNotice,
                    onCloseNoticeDialogs = noticeActions.onCloseDialogs
                )

                AnimatedVisibility(
                    visible = splashVisible,
                    exit = fadeOut(animationSpec = tween(600))
                ) {
                    SplashScreen()
                }
            }
        }
    }
}
