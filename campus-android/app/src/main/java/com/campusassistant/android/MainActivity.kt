package com.campusassistant.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusassistant.android.core.datastore.NoticeStore
import com.campusassistant.android.core.datastore.ServerConfigStore
import com.campusassistant.android.core.datastore.TokenDataStore
import com.campusassistant.android.core.network.NetworkModule
import com.campusassistant.android.core.network.ServerConfigHolder
import com.campusassistant.android.data.repository.AuthRepository
import com.campusassistant.android.data.repository.EmptyClassroomRepository
import com.campusassistant.android.data.repository.GradesRepository
import com.campusassistant.android.data.repository.PersonalizationRepository
import com.campusassistant.android.data.repository.ScheduleRepository
import com.campusassistant.android.data.repository.UserRepository
import com.campusassistant.android.ui.AppRoot
import com.campusassistant.android.ui.AppViewModel
import com.campusassistant.android.ui.AppViewModelFactory
import com.campusassistant.android.ui.emptyclassroom.EmptyClassroomViewModel
import com.campusassistant.android.ui.emptyclassroom.EmptyClassroomViewModelFactory
import com.campusassistant.android.ui.grades.GradesViewModel
import com.campusassistant.android.ui.grades.GradesViewModelFactory
import com.campusassistant.android.ui.notice.NoticeViewModel
import com.campusassistant.android.ui.notice.NoticeViewModelFactory
import com.campusassistant.android.ui.profile.ProfileViewModel
import com.campusassistant.android.ui.profile.ProfileViewModelFactory
import com.campusassistant.android.ui.schedule.ScheduleViewModel
import com.campusassistant.android.ui.schedule.ScheduleViewModelFactory
import com.campusassistant.android.ui.theme.CampusAssistantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serverConfigStore = ServerConfigStore(applicationContext)
        val noticeStore = NoticeStore(applicationContext)
        val tokenDataStore = TokenDataStore(applicationContext)
        val networkModule = NetworkModule(tokenDataStore)
        val authRepository = AuthRepository(networkModule.authApi, tokenDataStore)
        val noticeViewModelFactory = NoticeViewModelFactory(authRepository, noticeStore)
        val userRepository = UserRepository(networkModule.userApi, tokenDataStore)
        val scheduleRepository = ScheduleRepository(networkModule.scheduleApi, tokenDataStore)
        val gradesRepository = GradesRepository(networkModule.gradesApi, tokenDataStore)
        val emptyClassroomRepository = EmptyClassroomRepository(networkModule.emptyClassroomApi, tokenDataStore)
        val personalizationRepository = PersonalizationRepository(networkModule.personalizationApi, tokenDataStore)
        val appViewModelFactory = AppViewModelFactory(authRepository)
        val profileViewModelFactory = ProfileViewModelFactory(userRepository, personalizationRepository, serverConfigStore)
        val scheduleViewModelFactory = ScheduleViewModelFactory(authRepository, scheduleRepository)
        val gradesViewModelFactory = GradesViewModelFactory(gradesRepository)
        val emptyClassroomViewModelFactory = EmptyClassroomViewModelFactory(emptyClassroomRepository)

        setContent {
            val appViewModel: AppViewModel = viewModel(factory = appViewModelFactory)
            val noticeViewModel: NoticeViewModel = viewModel(factory = noticeViewModelFactory)
            val profileViewModel: ProfileViewModel = viewModel(factory = profileViewModelFactory)
            val scheduleViewModel: ScheduleViewModel = viewModel(factory = scheduleViewModelFactory)
            val gradesViewModel: GradesViewModel = viewModel(factory = gradesViewModelFactory)
            val emptyClassroomViewModel: EmptyClassroomViewModel = viewModel(factory = emptyClassroomViewModelFactory)
            val appState by appViewModel.uiState.collectAsStateWithLifecycle()
            val noticeState by noticeViewModel.uiState.collectAsStateWithLifecycle()
            val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
            val scheduleState by scheduleViewModel.uiState.collectAsStateWithLifecycle()
            val gradesState by gradesViewModel.uiState.collectAsStateWithLifecycle()
            val emptyClassroomState by emptyClassroomViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                ServerConfigHolder.update(serverConfigStore.getConfig())
                profileViewModel.loadServerSettings()
                noticeViewModel.start()
            }

            CampusAssistantTheme(
                globalFontEnabled = profileState.globalFontEnabled,
            ) {
                AppRoot(
                    appState = appState,
                    noticeState = noticeState,
                    profileState = profileState,
                    scheduleState = scheduleState,
                    gradesState = gradesState,
                    emptyClassroomState = emptyClassroomState,
                    onLogin = appViewModel::login,
                    onRefreshNotice = noticeViewModel::refreshNotice,
                    onOpenLatestNotice = noticeViewModel::openLatestNotice,
                    onOpenNoticeHistory = noticeViewModel::openHistoryDialog,
                    onOpenHistoryNotice = noticeViewModel::openHistoryNotice,
                    onCloseNoticeDialogs = noticeViewModel::closeDialogs,
                    onShowLoginPreview = appViewModel::showLoginPreview,
                    onHideLoginPreview = appViewModel::hideLoginPreview,
                    onLogout = appViewModel::logout,
                    onRefreshProfile = profileViewModel::loadProfile,
                    onAutoPunchChange = profileViewModel::updateAutoPunch,
                    onProfileAvatarSelect = profileViewModel::selectAvatar,
                    onProfileBackgroundSelect = profileViewModel::selectBackground,
                    onProfileWallpaperSelect = profileViewModel::selectWallpaper,
                    onProfileCardOpacityChange = profileViewModel::updateCardOpacity,
                    onProfileCardBlurChange = profileViewModel::updateCardBlur,
                    onProfileWallpaperMaskChange = profileViewModel::updateWallpaperMask,
                    onProfileGlobalFontChange = profileViewModel::updateGlobalFont,
                    onPersonalizationExpandedToggle = profileViewModel::togglePersonalizationExpanded,
                    onServerSettingsToggle = profileViewModel::toggleServerSettingsExpanded,
                    onServerHostChange = profileViewModel::updateServerHost,
                    onServerPortChange = profileViewModel::updateServerPort,
                    onSaveServerSettings = profileViewModel::saveServerSettings,
                    onSavePersonalization = profileViewModel::savePersonalization,
                    onProfileAssetUpload = profileViewModel::uploadAsset,
                    onDeleteAccount = profileViewModel::deleteAccount,
                    onRefreshSchedule = scheduleViewModel::loadSchedule,
                    onSyncSchedule = scheduleViewModel::syncUserDataAndReload,
                    onScheduleWeekModeChange = scheduleViewModel::setWeekMode,
                    onScheduleCourseClick = scheduleViewModel::selectCourse,
                    onDismissScheduleCourse = scheduleViewModel::dismissCourseDialog,
                    onShowOtherCourses = scheduleViewModel::showOtherCourses,
                    onDismissOtherCourses = scheduleViewModel::dismissOtherCourses,
                    onGradesYearChange = gradesViewModel::setAcademicYear,
                    onGradesYearIncrease = gradesViewModel::increaseYear,
                    onGradesYearDecrease = gradesViewModel::decreaseYear,
                    onGradesSemesterChange = gradesViewModel::setSemester,
                    onGradesSortChange = gradesViewModel::setSortMode,
                    onGradesViewModeChange = gradesViewModel::setViewMode,
                    onQueryGrades = gradesViewModel::queryGrades,
                    onSubmitGradeTask = gradesViewModel::submitLatestTask,
                    onEmptyClassroomYearChange = emptyClassroomViewModel::setAcademicYear,
                    onEmptyClassroomYearIncrease = emptyClassroomViewModel::increaseYear,
                    onEmptyClassroomYearDecrease = emptyClassroomViewModel::decreaseYear,
                    onEmptyClassroomSemesterChange = emptyClassroomViewModel::setSemester,
                    onEmptyClassroomDayChange = emptyClassroomViewModel::setDayOfWeek,
                    onEmptyClassroomWeekToggle = emptyClassroomViewModel::toggleWeek,
                    onEmptyClassroomPeriodToggle = emptyClassroomViewModel::togglePeriod,
                    onEmptyClassroomCampusChange = emptyClassroomViewModel::setCampus,
                    onEmptyClassroomBuildingChange = emptyClassroomViewModel::setBuilding,
                    onSubmitEmptyClassroomTask = emptyClassroomViewModel::submitTask,
                    onQueryEmptyClassroomResult = emptyClassroomViewModel::queryResult,
                    onResetEmptyClassroom = emptyClassroomViewModel::resetSelection,
                    onLoggedOut = {
                        profileViewModel.reset()
                        profileViewModel.loadServerSettings()
                        scheduleViewModel.reset()
                        gradesViewModel.reset()
                        emptyClassroomViewModel.reset()
                    }
                )
            }
        }
    }
}
