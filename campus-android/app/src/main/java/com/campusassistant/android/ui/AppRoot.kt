package com.campusassistant.android.ui

import androidx.compose.animation.AnimatedContent
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
import com.campusassistant.android.data.model.PublicNotice
import com.campusassistant.android.data.model.ScheduleCourse
import com.campusassistant.android.ui.common.CampusCardStyleProvider
import com.campusassistant.android.ui.emptyclassroom.EmptyClassroomUiState
import com.campusassistant.android.ui.grades.GradeSortMode
import com.campusassistant.android.ui.grades.GradeViewMode
import com.campusassistant.android.ui.grades.GradesUiState
import com.campusassistant.android.ui.login.LoginScreen
import com.campusassistant.android.ui.navigation.MainShell
import com.campusassistant.android.ui.notice.NoticeOverlay
import com.campusassistant.android.ui.notice.NoticeUiState
import com.campusassistant.android.ui.profile.ProfileUiState
import com.campusassistant.android.ui.schedule.ScheduleUiState
import com.campusassistant.android.ui.schedule.ScheduleWeekMode
import com.campusassistant.android.ui.splash.SplashScreen
import com.campusassistant.android.ui.text.AppTextProvider
import kotlinx.coroutines.delay
import okhttp3.MultipartBody

@Composable
fun AppRoot(
    appState: AppUiState,
    noticeState: NoticeUiState,
    profileState: ProfileUiState,
    scheduleState: ScheduleUiState,
    gradesState: GradesUiState,
    emptyClassroomState: EmptyClassroomUiState,
    onLogin: (String, String) -> Unit,
    onRefreshNotice: () -> Unit,
    onOpenLatestNotice: () -> Unit,
    onOpenNoticeHistory: () -> Unit,
    onOpenHistoryNotice: (PublicNotice) -> Unit,
    onCloseNoticeDialogs: () -> Unit,
    onShowLoginPreview: () -> Unit,
    onHideLoginPreview: () -> Unit,
    onLogout: () -> Unit,
    onRefreshProfile: () -> Unit,
    onAutoPunchChange: (Boolean) -> Unit,
    onProfileAvatarSelect: (String) -> Unit,
    onProfileBackgroundSelect: (String) -> Unit,
    onProfileWallpaperSelect: (String) -> Unit,
    onProfileCardOpacityChange: (Float) -> Unit,
    onProfileCardBlurChange: (Float) -> Unit,
    onProfileWallpaperMaskChange: (Float) -> Unit,
    onProfileGlobalFontChange: (Boolean) -> Unit,
    onPersonalizationExpandedToggle: () -> Unit,
    onServerSettingsToggle: () -> Unit,
    onServerHostChange: (String) -> Unit,
    onServerPortChange: (String) -> Unit,
    onSaveServerSettings: () -> Unit,
    onSavePersonalization: () -> Unit,
    onProfileAssetUpload: (String, MultipartBody.Part) -> Unit,
    onDeleteAccount: () -> Unit,
    onRefreshSchedule: () -> Unit,
    onSyncSchedule: () -> Unit,
    onScheduleWeekModeChange: (ScheduleWeekMode) -> Unit,
    onScheduleCourseClick: (ScheduleCourse) -> Unit,
    onDismissScheduleCourse: () -> Unit,
    onShowOtherCourses: () -> Unit,
    onDismissOtherCourses: () -> Unit,
    onGradesYearChange: (String) -> Unit,
    onGradesYearIncrease: () -> Unit,
    onGradesYearDecrease: () -> Unit,
    onGradesSemesterChange: (String) -> Unit,
    onGradesSortChange: (GradeSortMode) -> Unit,
    onGradesViewModeChange: (GradeViewMode) -> Unit,
    onQueryGrades: () -> Unit,
    onSubmitGradeTask: () -> Unit,
    onEmptyClassroomYearChange: (String) -> Unit,
    onEmptyClassroomYearIncrease: () -> Unit,
    onEmptyClassroomYearDecrease: () -> Unit,
    onEmptyClassroomSemesterChange: (String) -> Unit,
    onEmptyClassroomDayChange: (String) -> Unit,
    onEmptyClassroomWeekToggle: (Int) -> Unit,
    onEmptyClassroomPeriodToggle: (Int) -> Unit,
    onEmptyClassroomCampusChange: (String) -> Unit,
    onEmptyClassroomBuildingChange: (String) -> Unit,
    onSubmitEmptyClassroomTask: () -> Unit,
    onQueryEmptyClassroomResult: () -> Unit,
    onResetEmptyClassroom: () -> Unit,
    onLoggedOut: () -> Unit
) {
    var splashVisible by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1700)
        splashVisible = false
        onRefreshNotice()
    }

    LaunchedEffect(appState.authState, appState.sessionVersion) {
        when (appState.authState) {
            AuthState.LoggedIn -> {
                onRefreshProfile()
                onRefreshSchedule()
            }
            AuthState.LoggedOut -> onLoggedOut()
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
                if (splashVisible) {
                    SplashScreen()
                    return@CampusCardStyleProvider
                }

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
                                onServerSettingsToggle = onServerSettingsToggle,
                                onServerHostChange = onServerHostChange,
                                onServerPortChange = onServerPortChange,
                                onSaveServerSettings = onSaveServerSettings,
                                showBackToApp = appState.tokenExists && loginPreviewVisible,
                                onBackToApp = onHideLoginPreview,
                                onLogin = onLogin
                            )
                        }

                        loginPreviewVisible -> {
                            LoginScreen(
                                loading = appState.loginLoading,
                                errorMessage = appState.loginError,
                                serverSettingsDraft = profileState.serverSettingsDraft,
                                savingServerConfig = profileState.savingServerConfig,
                                onServerSettingsToggle = onServerSettingsToggle,
                                onServerHostChange = onServerHostChange,
                                onServerPortChange = onServerPortChange,
                                onSaveServerSettings = onSaveServerSettings,
                                showBackToApp = true,
                                onBackToApp = onHideLoginPreview,
                                onLogin = onLogin
                            )
                        }

                        else -> {
                            MainShell(
                                tokenExists = appState.tokenExists,
                                profileState = profileState,
                                scheduleState = scheduleState,
                                gradesState = gradesState,
                                emptyClassroomState = emptyClassroomState,
                                onShowLoginPreview = onShowLoginPreview,
                                onLogout = onLogout,
                                onRefreshProfile = onRefreshProfile,
                                onAutoPunchChange = onAutoPunchChange,
                                onProfileAvatarSelect = onProfileAvatarSelect,
                                onProfileBackgroundSelect = onProfileBackgroundSelect,
                                onProfileWallpaperSelect = onProfileWallpaperSelect,
                                onProfileCardOpacityChange = onProfileCardOpacityChange,
                                onProfileCardBlurChange = onProfileCardBlurChange,
                                onProfileWallpaperMaskChange = onProfileWallpaperMaskChange,
                                onProfileGlobalFontChange = onProfileGlobalFontChange,
                                onPersonalizationExpandedToggle = onPersonalizationExpandedToggle,
                                onServerSettingsToggle = onServerSettingsToggle,
                                onServerHostChange = onServerHostChange,
                                onServerPortChange = onServerPortChange,
                                onSaveServerSettings = onSaveServerSettings,
                                onSavePersonalization = onSavePersonalization,
                                onProfileAssetUpload = onProfileAssetUpload,
                                onDeleteAccount = onDeleteAccount,
                                onRefreshSchedule = onRefreshSchedule,
                                onSyncSchedule = onSyncSchedule,
                                onScheduleWeekModeChange = onScheduleWeekModeChange,
                                onScheduleCourseClick = onScheduleCourseClick,
                                onDismissScheduleCourse = onDismissScheduleCourse,
                                onShowOtherCourses = onShowOtherCourses,
                                onDismissOtherCourses = onDismissOtherCourses,
                                onGradesYearChange = onGradesYearChange,
                                onGradesYearIncrease = onGradesYearIncrease,
                                onGradesYearDecrease = onGradesYearDecrease,
                                onGradesSemesterChange = onGradesSemesterChange,
                                onGradesSortChange = onGradesSortChange,
                                onGradesViewModeChange = onGradesViewModeChange,
                                onQueryGrades = onQueryGrades,
                                onSubmitGradeTask = onSubmitGradeTask,
                                onEmptyClassroomYearChange = onEmptyClassroomYearChange,
                                onEmptyClassroomYearIncrease = onEmptyClassroomYearIncrease,
                                onEmptyClassroomYearDecrease = onEmptyClassroomYearDecrease,
                                onEmptyClassroomSemesterChange = onEmptyClassroomSemesterChange,
                                onEmptyClassroomDayChange = onEmptyClassroomDayChange,
                                onEmptyClassroomWeekToggle = onEmptyClassroomWeekToggle,
                                onEmptyClassroomPeriodToggle = onEmptyClassroomPeriodToggle,
                                onEmptyClassroomCampusChange = onEmptyClassroomCampusChange,
                                onEmptyClassroomBuildingChange = onEmptyClassroomBuildingChange,
                                onSubmitEmptyClassroomTask = onSubmitEmptyClassroomTask,
                                onQueryEmptyClassroomResult = onQueryEmptyClassroomResult,
                                onResetEmptyClassroom = onResetEmptyClassroom
                            )
                        }
                    }
                }

                NoticeOverlay(
                    state = noticeState,
                    onOpenLatestNotice = onOpenLatestNotice,
                    onOpenNoticeHistory = onOpenNoticeHistory,
                    onOpenHistoryNotice = onOpenHistoryNotice,
                    onCloseNoticeDialogs = onCloseNoticeDialogs
                )
            }
        }
    }
}
