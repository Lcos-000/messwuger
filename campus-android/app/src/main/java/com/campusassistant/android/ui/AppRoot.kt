package com.campusassistant.android.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.campusassistant.android.data.model.PublicNotice
import com.campusassistant.android.data.model.ScheduleCourse
import com.campusassistant.android.ui.common.CampusCardStyleProvider
import com.campusassistant.android.ui.emptyclassroom.EmptyClassroomScreen
import com.campusassistant.android.ui.emptyclassroom.EmptyClassroomUiState
import com.campusassistant.android.ui.grades.GradeSortMode
import com.campusassistant.android.ui.grades.GradeViewMode
import com.campusassistant.android.ui.grades.GradesScreen
import com.campusassistant.android.ui.grades.GradesUiState
import com.campusassistant.android.ui.login.LoginScreen
import com.campusassistant.android.ui.navigation.MainTab
import com.campusassistant.android.ui.notice.NoticeUiState
import com.campusassistant.android.ui.profile.ProfileScreen
import com.campusassistant.android.ui.profile.ProfileUiState
import com.campusassistant.android.ui.profile.resolveAssetUrl
import com.campusassistant.android.ui.schedule.ScheduleScreen
import com.campusassistant.android.ui.schedule.ScheduleUiState
import com.campusassistant.android.ui.schedule.ScheduleWeekMode
import com.campusassistant.android.ui.splash.SplashScreen
import com.campusassistant.android.ui.text.AppTextProvider
import com.campusassistant.android.ui.text.LocalAppText
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
                        authState == AuthState.Checking -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        authState == AuthState.LoggedOut -> LoginScreen(
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
                        loginPreviewVisible -> LoginScreen(
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
                        else -> MainScreen(
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
                NoticeEntryButton(
                    hasUnread = noticeState.hasUnread,
                    hasNotice = noticeState.latestNotice?.content?.isNotBlank() == true,
                    onClick = onOpenLatestNotice
                )
                if (noticeState.showNoticeDialog && noticeState.activeNotice != null) {
                    NoticeDetailDialog(
                        notice = noticeState.activeNotice,
                        onDismiss = onCloseNoticeDialogs,
                        onShowHistory = onOpenNoticeHistory
                    )
                }
                if (noticeState.showHistoryDialog) {
                    NoticeHistoryDialog(
                        notices = noticeState.history,
                        onDismiss = onCloseNoticeDialogs,
                        onNoticeClick = onOpenHistoryNotice
                    )
                }
            }
        }
    }
}

@Composable
private fun NoticeEntryButton(
    hasUnread: Boolean,
    hasNotice: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 72.dp, end = 18.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            IconButton(onClick = onClick, enabled = hasNotice) {
                BadgedBox(
                    badge = {
                        if (hasUnread) {
                            Badge()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = LocalAppText.current.common.notice,
                        tint = if (hasNotice) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.36f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NoticeDetailDialog(
    notice: PublicNotice,
    onDismiss: () -> Unit,
    onShowHistory: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = notice.title?.ifBlank { LocalAppText.current.common.notice } ?: LocalAppText.current.common.notice,
                    style = MaterialTheme.typography.titleMedium
                )
                if (!notice.updatedAt.isNullOrBlank()) {
                    Text(
                        text = notice.updatedAt.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!notice.level.isNullOrBlank()) {
                    Text(
                        text = notice.level.orEmpty().uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = notice.content.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onShowHistory) {
                Text("查看历史")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LocalAppText.current.common.close)
            }
        }
    )
}

@Composable
private fun NoticeHistoryDialog(
    notices: List<PublicNotice>,
    onDismiss: () -> Unit,
    onNoticeClick: (PublicNotice) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("历史公告") },
        text = {
            if (notices.isEmpty()) {
                Text("暂无历史公告", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    notices.forEach { notice ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onNoticeClick(notice) },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                                Text(
                                    text = notice.title?.ifBlank { LocalAppText.current.common.notice } ?: LocalAppText.current.common.notice,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                if (!notice.updatedAt.isNullOrBlank()) {
                                    Text(
                                        text = notice.updatedAt.orEmpty(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Text(
                                    text = notice.content.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(LocalAppText.current.common.close)
            }
        }
    )
}

@Composable
private fun GlobalWallpaperLayer(profileState: ProfileUiState) {
    val draft = profileState.personalizationDraft
    val wallpaperUrl = resolveAssetUrl(draft.wallpaper)
    val mask = draft.wallpaperMask.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF7F9FC),
                        Color(0xFFE9EEF6),
                        Color(0xFFF4F7FB)
                    )
                )
            )
    )
    if (wallpaperUrl != null) {
        AsyncImage(
            model = wallpaperUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 1f - mask
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFF7F9FC).copy(alpha = mask * 0.94f),
                            Color(0xFFE9EEF6).copy(alpha = mask * 0.46f),
                            Color(0xFFF4F7FB).copy(alpha = mask * 0.78f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun MainScreen(
    tokenExists: Boolean,
    profileState: ProfileUiState,
    scheduleState: ScheduleUiState,
    gradesState: GradesUiState,
    emptyClassroomState: EmptyClassroomUiState,
    onShowLoginPreview: () -> Unit,
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
    onResetEmptyClassroom: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Schedule) }
    val navText = LocalAppText.current.nav

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                    .height(72.dp)
                    .clip(RoundedCornerShape(26.dp)),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                tonalElevation = NavigationBarDefaults.Elevation + 4.dp
            ) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title(navText)) },
                        label = { Text(tab.title(navText)) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    ContentTransform(
                        targetContentEnter = slideInHorizontally(
                            animationSpec = tween(260),
                            initialOffsetX = { width -> if (forward) width / 7 else -width / 7 }
                        ) + fadeIn(animationSpec = tween(220)),
                        initialContentExit = slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { width -> if (forward) -width / 10 else width / 10 }
                        ) + fadeOut(animationSpec = tween(180))
                    )
                },
                label = "tab-transition"
            ) { currentTab ->
                when (currentTab) {
                    MainTab.Schedule -> ScheduleScreen(
                        state = scheduleState,
                        onRefresh = onSyncSchedule,
                        onWeekModeChange = onScheduleWeekModeChange,
                        onCourseClick = onScheduleCourseClick,
                        onDismissCourse = onDismissScheduleCourse,
                        onShowOtherCourses = onShowOtherCourses,
                        onDismissOtherCourses = onDismissOtherCourses
                    )
                    MainTab.Grades -> GradesScreen(
                        state = gradesState,
                        onAcademicYearChange = onGradesYearChange,
                        onYearIncrease = onGradesYearIncrease,
                        onYearDecrease = onGradesYearDecrease,
                        onSemesterChange = onGradesSemesterChange,
                        onSortModeChange = onGradesSortChange,
                        onViewModeChange = onGradesViewModeChange,
                        onQueryGrades = onQueryGrades,
                        onSubmitGradeTask = onSubmitGradeTask
                    )
                    MainTab.EmptyClassroom -> EmptyClassroomScreen(
                        state = emptyClassroomState,
                        onAcademicYearChange = onEmptyClassroomYearChange,
                        onYearIncrease = onEmptyClassroomYearIncrease,
                        onYearDecrease = onEmptyClassroomYearDecrease,
                        onSemesterChange = onEmptyClassroomSemesterChange,
                        onDayChange = onEmptyClassroomDayChange,
                        onWeekToggle = onEmptyClassroomWeekToggle,
                        onPeriodToggle = onEmptyClassroomPeriodToggle,
                        onCampusChange = onEmptyClassroomCampusChange,
                        onBuildingChange = onEmptyClassroomBuildingChange,
                        onSubmitTask = onSubmitEmptyClassroomTask,
                        onQueryResult = onQueryEmptyClassroomResult,
                        onReset = onResetEmptyClassroom
                    )
                    MainTab.Profile -> ProfileScreen(
                        tokenExists = tokenExists,
                        profileState = profileState,
                        onRefresh = onRefreshProfile,
                        onAutoPunchChange = onAutoPunchChange,
                        onAvatarSelect = onProfileAvatarSelect,
                        onBackgroundSelect = onProfileBackgroundSelect,
                        onWallpaperSelect = onProfileWallpaperSelect,
                        onCardOpacityChange = onProfileCardOpacityChange,
                        onCardBlurChange = onProfileCardBlurChange,
                        onWallpaperMaskChange = onProfileWallpaperMaskChange,
                        onGlobalFontChange = onProfileGlobalFontChange,
                        onPersonalizationExpandedToggle = onPersonalizationExpandedToggle,
                        onServerSettingsToggle = onServerSettingsToggle,
                        onServerHostChange = onServerHostChange,
                        onServerPortChange = onServerPortChange,
                        onSaveServerSettings = onSaveServerSettings,
                        onSavePersonalization = onSavePersonalization,
                        onAssetUpload = onProfileAssetUpload,
                        onDeleteAccount = onDeleteAccount,
                        onShowLoginPreview = onShowLoginPreview,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}
