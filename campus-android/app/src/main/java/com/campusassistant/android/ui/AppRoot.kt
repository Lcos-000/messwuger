package com.campusassistant.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import okhttp3.MultipartBody
import com.campusassistant.android.ui.common.CampusCardStyleProvider
import com.campusassistant.android.ui.emptyclassroom.EmptyClassroomScreen
import com.campusassistant.android.ui.emptyclassroom.EmptyClassroomUiState
import com.campusassistant.android.ui.grades.GradesScreen
import com.campusassistant.android.ui.grades.GradeSortMode
import com.campusassistant.android.ui.grades.GradeViewMode
import com.campusassistant.android.ui.grades.GradesUiState
import com.campusassistant.android.ui.login.LoginScreen
import com.campusassistant.android.ui.navigation.MainTab
import com.campusassistant.android.ui.profile.ProfileScreen
import com.campusassistant.android.ui.profile.ProfileUiState
import com.campusassistant.android.ui.profile.resolveAssetUrl
import com.campusassistant.android.ui.schedule.ScheduleScreen
import com.campusassistant.android.ui.schedule.ScheduleUiState
import com.campusassistant.android.ui.schedule.ScheduleWeekMode
import com.campusassistant.android.ui.splash.SplashScreen
import com.campusassistant.android.ui.text.AppTextProvider
import com.campusassistant.android.ui.text.LocalAppText
import com.campusassistant.android.data.model.ScheduleCourse

@Composable
fun AppRoot(
    appState: AppUiState,
    profileState: ProfileUiState,
    scheduleState: ScheduleUiState,
    gradesState: GradesUiState,
    emptyClassroomState: EmptyClassroomUiState,
    onLogin: (String, String) -> Unit,
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
    }

    LaunchedEffect(appState.authState) {
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
                targetState = appState.authState,
                transitionSpec = {
                    ContentTransform(
                        targetContentEnter = fadeIn(animationSpec = tween(220)),
                        initialContentExit = fadeOut(animationSpec = tween(180))
                    )
                },
                label = "auth-transition"
            ) { authState ->
                when (authState) {
                    AuthState.Checking -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    AuthState.LoggedOut -> LoginScreen(
                        loading = appState.loginLoading,
                        errorMessage = appState.loginError,
                        serverSettingsDraft = profileState.serverSettingsDraft,
                        savingServerConfig = profileState.savingServerConfig,
                        onServerSettingsToggle = onServerSettingsToggle,
                        onServerHostChange = onServerHostChange,
                        onServerPortChange = onServerPortChange,
                        onSaveServerSettings = onSaveServerSettings,
                        onLogin = onLogin
                    )
                    AuthState.LoggedIn -> MainScreen(
                        tokenExists = appState.tokenExists,
                        profileState = profileState,
                        scheduleState = scheduleState,
                        gradesState = gradesState,
                        emptyClassroomState = emptyClassroomState,
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
        }
    }
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
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}
