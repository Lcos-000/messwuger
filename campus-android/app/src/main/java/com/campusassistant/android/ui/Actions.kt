package com.campusassistant.android.ui

import com.campusassistant.android.data.model.PublicNotice
import com.campusassistant.android.data.model.ScheduleCourse
import com.campusassistant.android.ui.grades.GradeSortMode
import com.campusassistant.android.ui.grades.GradeViewMode
import com.campusassistant.android.ui.schedule.ScheduleWeekMode
import okhttp3.MultipartBody

data class AuthActions(
    val onLogin: (String, String) -> Unit,
    val onLogout: () -> Unit,
    val onShowLoginPreview: () -> Unit,
    val onHideLoginPreview: () -> Unit,
    val onLoggedOut: () -> Unit
)

data class NoticeActions(
    val onRefresh: () -> Unit,
    val onOpenLatest: () -> Unit,
    val onOpenHistory: () -> Unit,
    val onOpenHistoryNotice: (PublicNotice) -> Unit,
    val onCloseDialogs: () -> Unit
)

data class ProfileActions(
    val onRefresh: () -> Unit,
    val onAutoPunchChange: (Boolean) -> Unit,
    val onAvatarSelect: (String) -> Unit,
    val onBackgroundSelect: (String) -> Unit,
    val onWallpaperSelect: (String) -> Unit,
    val onCardOpacityChange: (Float) -> Unit,
    val onCardBlurChange: (Float) -> Unit,
    val onWallpaperMaskChange: (Float) -> Unit,
    val onGlobalFontChange: (Boolean) -> Unit,
    val onPersonalizationExpandedToggle: () -> Unit,
    val onServerSettingsToggle: () -> Unit,
    val onServerHostChange: (String) -> Unit,
    val onServerPortChange: (String) -> Unit,
    val onSaveServerSettings: () -> Unit,
    val onSavePersonalization: () -> Unit,
    val onAssetUpload: (String, MultipartBody.Part) -> Unit,
    val onDeleteAccount: () -> Unit
)

data class ScheduleActions(
    val onRefresh: () -> Unit,
    val onSync: () -> Unit,
    val onWeekModeChange: (ScheduleWeekMode) -> Unit,
    val onCourseClick: (ScheduleCourse) -> Unit,
    val onDismissCourse: () -> Unit,
    val onShowOtherCourses: () -> Unit,
    val onDismissOtherCourses: () -> Unit
)

data class GradesActions(
    val onYearChange: (String) -> Unit,
    val onYearIncrease: () -> Unit,
    val onYearDecrease: () -> Unit,
    val onSemesterChange: (String) -> Unit,
    val onSortChange: (GradeSortMode) -> Unit,
    val onViewModeChange: (GradeViewMode) -> Unit,
    val onQuery: () -> Unit,
    val onSubmitTask: () -> Unit
)

data class EmptyClassroomActions(
    val onYearChange: (String) -> Unit,
    val onYearIncrease: () -> Unit,
    val onYearDecrease: () -> Unit,
    val onSemesterChange: (String) -> Unit,
    val onDayChange: (String) -> Unit,
    val onWeekToggle: (Int) -> Unit,
    val onPeriodToggle: (Int) -> Unit,
    val onCampusChange: (String) -> Unit,
    val onBuildingChange: (String) -> Unit,
    val onSubmitTask: () -> Unit,
    val onQueryResult: () -> Unit,
    val onReset: () -> Unit
)
