package com.campusassistant.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import okhttp3.MultipartBody
import com.campusassistant.android.ui.common.CampusCardStyleProvider
import com.campusassistant.android.ui.emptyclassroom.EmptyClassroomScreen
import com.campusassistant.android.ui.emptyclassroom.EmptyClassroomUiState
import com.campusassistant.android.ui.grades.GradesScreen
import com.campusassistant.android.ui.grades.GradeSortMode
import com.campusassistant.android.ui.grades.GradesUiState
import com.campusassistant.android.ui.login.LoginScreen
import com.campusassistant.android.ui.navigation.MainTab
import com.campusassistant.android.ui.profile.ProfileScreen
import com.campusassistant.android.ui.profile.ProfileUiState
import com.campusassistant.android.ui.profile.resolveAssetUrl
import com.campusassistant.android.ui.schedule.ScheduleScreen
import com.campusassistant.android.ui.schedule.ScheduleUiState
import com.campusassistant.android.ui.schedule.ScheduleWeekMode
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
    onSavePersonalization: () -> Unit,
    onProfileAssetUpload: (String, MultipartBody.Part) -> Unit,
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
        CampusCardStyleProvider(
            opacity = profileState.personalizationDraft.cardOpacity,
            blur = profileState.personalizationDraft.cardBlur
        ) {
            when (appState.authState) {
            AuthState.Checking -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            AuthState.LoggedOut -> LoginScreen(
                loading = appState.loginLoading,
                errorMessage = appState.loginError,
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
                onSavePersonalization = onSavePersonalization,
                onProfileAssetUpload = onProfileAssetUpload,
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

@Composable
private fun GlobalWallpaperLayer(profileState: ProfileUiState) {
    val draft = profileState.personalizationDraft
    val wallpaperUrl = resolveAssetUrl(draft.wallpaper)
    val mask = draft.wallpaperMask.coerceIn(0f, 1f)
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE9EEF8)))
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
                            Color(0xFFE9EEF8).copy(alpha = mask * 0.9f),
                            Color(0xFFE9EEF8).copy(alpha = mask * 0.36f),
                            Color(0xFFE9EEF8).copy(alpha = mask * 0.72f)
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
    onSavePersonalization: () -> Unit,
    onProfileAssetUpload: (String, MultipartBody.Part) -> Unit,
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

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
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
                    onSavePersonalization = onSavePersonalization,
                    onAssetUpload = onProfileAssetUpload,
                    onLogout = onLogout
                )
            }
        }
    }
}
