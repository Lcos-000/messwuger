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
import com.campusassistant.android.ui.AuthActions
import com.campusassistant.android.ui.EmptyClassroomActions
import com.campusassistant.android.ui.GradesActions
import com.campusassistant.android.ui.NoticeActions
import com.campusassistant.android.ui.ProfileActions
import com.campusassistant.android.ui.ScheduleActions
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
        val profileViewModelFactory = ProfileViewModelFactory(userRepository, personalizationRepository, serverConfigStore, authRepository)
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
                    authActions = AuthActions(
                        onLogin = appViewModel::login,
                        onLogout = appViewModel::logout,
                        onShowLoginPreview = appViewModel::showLoginPreview,
                        onHideLoginPreview = appViewModel::hideLoginPreview,
                        onLoggedOut = {
                            profileViewModel.reset()
                            profileViewModel.loadServerSettings()
                            scheduleViewModel.reset()
                            gradesViewModel.reset()
                            emptyClassroomViewModel.reset()
                        }
                    ),
                    noticeActions = NoticeActions(
                        onRefresh = noticeViewModel::refreshNotice,
                        onOpenLatest = noticeViewModel::openLatestNotice,
                        onOpenHistory = noticeViewModel::openHistoryDialog,
                        onOpenHistoryNotice = noticeViewModel::openHistoryNotice,
                        onCloseDialogs = noticeViewModel::closeDialogs
                    ),
                    profileActions = ProfileActions(
                        onRefresh = profileViewModel::loadProfile,
                        onAutoPunchChange = profileViewModel::updateAutoPunch,
                        onAvatarSelect = profileViewModel::selectAvatar,
                        onBackgroundSelect = profileViewModel::selectBackground,
                        onWallpaperSelect = profileViewModel::selectWallpaper,
                        onCardOpacityChange = profileViewModel::updateCardOpacity,
                        onCardBlurChange = profileViewModel::updateCardBlur,
                        onWallpaperMaskChange = profileViewModel::updateWallpaperMask,
                        onGlobalFontChange = profileViewModel::updateGlobalFont,
                        onPersonalizationExpandedToggle = profileViewModel::togglePersonalizationExpanded,
                        onServerSettingsToggle = profileViewModel::toggleServerSettingsExpanded,
                        onServerHostChange = profileViewModel::updateServerHost,
                        onServerPortChange = profileViewModel::updateServerPort,
                        onSaveServerSettings = profileViewModel::saveServerSettings,
                        onSavePersonalization = profileViewModel::savePersonalization,
                        onAssetUpload = profileViewModel::uploadAsset,
                        onDeleteAccount = profileViewModel::deleteAccount
                    ),
                    scheduleActions = ScheduleActions(
                        onRefresh = scheduleViewModel::loadSchedule,
                        onSync = scheduleViewModel::syncUserDataAndReload,
                        onWeekModeChange = scheduleViewModel::setWeekMode,
                        onCourseClick = scheduleViewModel::selectCourse,
                        onDismissCourse = scheduleViewModel::dismissCourseDialog,
                        onShowOtherCourses = scheduleViewModel::showOtherCourses,
                        onDismissOtherCourses = scheduleViewModel::dismissOtherCourses
                    ),
                    gradesActions = GradesActions(
                        onYearChange = gradesViewModel::setAcademicYear,
                        onYearIncrease = gradesViewModel::increaseYear,
                        onYearDecrease = gradesViewModel::decreaseYear,
                        onSemesterChange = gradesViewModel::setSemester,
                        onSortChange = gradesViewModel::setSortMode,
                        onViewModeChange = gradesViewModel::setViewMode,
                        onQuery = gradesViewModel::queryGrades,
                        onSubmitTask = gradesViewModel::submitLatestTask
                    ),
                    emptyClassroomActions = EmptyClassroomActions(
                        onYearChange = emptyClassroomViewModel::setAcademicYear,
                        onYearIncrease = emptyClassroomViewModel::increaseYear,
                        onYearDecrease = emptyClassroomViewModel::decreaseYear,
                        onSemesterChange = emptyClassroomViewModel::setSemester,
                        onDayChange = emptyClassroomViewModel::setDayOfWeek,
                        onWeekToggle = emptyClassroomViewModel::toggleWeek,
                        onPeriodToggle = emptyClassroomViewModel::togglePeriod,
                        onCampusChange = emptyClassroomViewModel::setCampus,
                        onBuildingChange = emptyClassroomViewModel::setBuilding,
                        onSubmitTask = emptyClassroomViewModel::submitTask,
                        onQueryResult = emptyClassroomViewModel::queryResult,
                        onReset = emptyClassroomViewModel::resetSelection
                    )
                )
            }
        }
    }
}
