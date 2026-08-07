package com.campusassistant.android.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.campusassistant.android.data.model.OtherScheduleCourse
import com.campusassistant.android.data.model.ScheduleCourse
import com.campusassistant.android.data.model.ScheduleResponse
import com.campusassistant.android.data.repository.AuthRepository
import com.campusassistant.android.data.repository.ScheduleRepository
import com.campusassistant.android.data.repository.calculateCurrentWeek
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScheduleWeekMode {
    Current,
    All
}

data class ScheduleUiState(
    val loading: Boolean = false,
    val schedule: ScheduleResponse? = null,
    val visibleCourses: List<ScheduleCourse> = emptyList(),
    val allCourses: List<ScheduleCourse> = emptyList(),
    val otherCourses: List<OtherScheduleCourse> = emptyList(),
    val selectedCourse: ScheduleCourse? = null,
    val showOtherCourses: Boolean = false,
    val weekMode: ScheduleWeekMode = ScheduleWeekMode.Current,
    val currentWeek: Int = 1,
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean
        get() = !loading && visibleCourses.isEmpty()
}

class ScheduleViewModel(
    private val authRepository: AuthRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    fun reset() {
        _uiState.value = ScheduleUiState()
    }

    fun loadSchedule() {
        if (_uiState.value.loading) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null, statusMessage = null) }
            scheduleRepository.getSchedule()
                .onSuccess { parsed ->
                    val currentWeek = calculateCurrentWeek(parsed.schedule?.semester)
                    val state = _uiState.value.copy(
                        loading = false,
                        schedule = parsed.schedule,
                        allCourses = parsed.courses,
                        otherCourses = parsed.otherCourses,
                        currentWeek = currentWeek,
                        errorMessage = null
                    )
                    _uiState.value = state.withVisibleCourses()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            errorMessage = throwable.message ?: "加载课表失败"
                        )
                    }
                }
        }
    }

    fun syncUserDataAndReload() {
        if (_uiState.value.loading) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null, statusMessage = null) }
            authRepository.refreshUserData()
                .onSuccess { message ->
                    _uiState.update { it.copy(statusMessage = message, loading = false) }
                    loadSchedule()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            errorMessage = throwable.message ?: "提交同步任务失败"
                        )
                    }
                }
        }
    }

    fun setWeekMode(mode: ScheduleWeekMode) {
        _uiState.update { it.copy(weekMode = mode) }
        _uiState.update { it.withVisibleCourses() }
    }

    fun selectCourse(course: ScheduleCourse) {
        _uiState.update { it.copy(selectedCourse = course) }
    }

    fun dismissCourseDialog() {
        _uiState.update { it.copy(selectedCourse = null) }
    }

    fun showOtherCourses() {
        _uiState.update { it.copy(showOtherCourses = true) }
    }

    fun dismissOtherCourses() {
        _uiState.update { it.copy(showOtherCourses = false) }
    }

    private fun ScheduleUiState.withVisibleCourses(): ScheduleUiState {
        val nextVisible = when (weekMode) {
            ScheduleWeekMode.All -> allCourses
            ScheduleWeekMode.Current -> allCourses.filter { it.weekNumbers.contains(currentWeek) }
        }
        return copy(visibleCourses = nextVisible)
    }
}

class ScheduleViewModelFactory(
    private val authRepository: AuthRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            return ScheduleViewModel(authRepository, scheduleRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
