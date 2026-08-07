package com.campusassistant.android.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.campusassistant.android.ui.emptyclassroom.EmptyClassroomScreen
import com.campusassistant.android.ui.emptyclassroom.EmptyClassroomUiState
import com.campusassistant.android.ui.grades.GradesScreen
import com.campusassistant.android.ui.grades.GradesUiState
import com.campusassistant.android.ui.profile.ProfileScreen
import com.campusassistant.android.ui.profile.ProfileUiState
import com.campusassistant.android.ui.schedule.ScheduleScreen
import com.campusassistant.android.ui.schedule.ScheduleUiState
import com.campusassistant.android.ui.text.LocalAppText
import com.campusassistant.android.ui.EmptyClassroomActions
import com.campusassistant.android.ui.GradesActions
import com.campusassistant.android.ui.ProfileActions
import com.campusassistant.android.ui.ScheduleActions

@Composable
fun MainShell(
    tokenExists: Boolean,
    profileState: ProfileUiState,
    scheduleState: ScheduleUiState,
    gradesState: GradesUiState,
    emptyClassroomState: EmptyClassroomUiState,
    onShowLoginPreview: () -> Unit,
    onLogout: () -> Unit,
    profileActions: ProfileActions,
    scheduleActions: ScheduleActions,
    gradesActions: GradesActions,
    emptyClassroomActions: EmptyClassroomActions
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
                    ContentTransform(
                        targetContentEnter = fadeIn(animationSpec = tween(380)),
                        initialContentExit = fadeOut(animationSpec = tween(320))
                    )
                },
                label = "tab-transition"
            ) { currentTab ->
                when (currentTab) {
                    MainTab.Schedule -> ScheduleScreen(
                        state = scheduleState,
                        onRefresh = scheduleActions.onSync,
                        onWeekModeChange = scheduleActions.onWeekModeChange,
                        onCourseClick = scheduleActions.onCourseClick,
                        onDismissCourse = scheduleActions.onDismissCourse,
                        onShowOtherCourses = scheduleActions.onShowOtherCourses,
                        onDismissOtherCourses = scheduleActions.onDismissOtherCourses
                    )

                    MainTab.Grades -> GradesScreen(
                        state = gradesState,
                        onAcademicYearChange = gradesActions.onYearChange,
                        onYearIncrease = gradesActions.onYearIncrease,
                        onYearDecrease = gradesActions.onYearDecrease,
                        onSemesterChange = gradesActions.onSemesterChange,
                        onSortModeChange = gradesActions.onSortChange,
                        onViewModeChange = gradesActions.onViewModeChange,
                        onQueryGrades = gradesActions.onQuery,
                        onSubmitGradeTask = gradesActions.onSubmitTask
                    )

                    MainTab.EmptyClassroom -> EmptyClassroomScreen(
                        state = emptyClassroomState,
                        onAcademicYearChange = emptyClassroomActions.onYearChange,
                        onYearIncrease = emptyClassroomActions.onYearIncrease,
                        onYearDecrease = emptyClassroomActions.onYearDecrease,
                        onSemesterChange = emptyClassroomActions.onSemesterChange,
                        onDayChange = emptyClassroomActions.onDayChange,
                        onWeekToggle = emptyClassroomActions.onWeekToggle,
                        onPeriodToggle = emptyClassroomActions.onPeriodToggle,
                        onCampusChange = emptyClassroomActions.onCampusChange,
                        onBuildingChange = emptyClassroomActions.onBuildingChange,
                        onSubmitTask = emptyClassroomActions.onSubmitTask,
                        onQueryResult = emptyClassroomActions.onQueryResult,
                        onReset = emptyClassroomActions.onReset
                    )

                    MainTab.Profile -> ProfileScreen(
                        tokenExists = tokenExists,
                        profileState = profileState,
                        onRefresh = profileActions.onRefresh,
                        onAutoPunchChange = profileActions.onAutoPunchChange,
                        onAvatarSelect = profileActions.onAvatarSelect,
                        onBackgroundSelect = profileActions.onBackgroundSelect,
                        onWallpaperSelect = profileActions.onWallpaperSelect,
                        onCardOpacityChange = profileActions.onCardOpacityChange,
                        onCardBlurChange = profileActions.onCardBlurChange,
                        onWallpaperMaskChange = profileActions.onWallpaperMaskChange,
                        onGlobalFontChange = profileActions.onGlobalFontChange,
                        onPersonalizationExpandedToggle = profileActions.onPersonalizationExpandedToggle,
                        onServerSettingsToggle = profileActions.onServerSettingsToggle,
                        onServerHostChange = profileActions.onServerHostChange,
                        onServerPortChange = profileActions.onServerPortChange,
                        onSaveServerSettings = profileActions.onSaveServerSettings,
                        onSavePersonalization = profileActions.onSavePersonalization,
                        onAssetUpload = profileActions.onAssetUpload,
                        onDeleteAccount = profileActions.onDeleteAccount,
                        onShowLoginPreview = onShowLoginPreview,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}
