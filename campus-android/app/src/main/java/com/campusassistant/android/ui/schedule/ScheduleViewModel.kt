package com.campusassistant.android.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.campusassistant.android.data.model.ParsedSchedule
import com.campusassistant.android.data.model.OtherScheduleCourse
import com.campusassistant.android.data.model.ScheduleCourse
import com.campusassistant.android.data.repository.AuthRepository
import com.campusassistant.android.data.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class ScheduleWeekMode {
    Current,
    All
}

data class ScheduleUiState(
    val loading: Boolean = false,
    val schedule: ParsedSchedule? = null,
    val currentWeek: Int = calculateCurrentWeek(),
    val weekMode: ScheduleWeekMode = ScheduleWeekMode.Current,
    val selectedCourse: ScheduleCourse? = null,
    val showOtherCourses: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    val visibleCourses: List<ScheduleCourse>
        get() {
            val courses = schedule?.courses.orEmpty()
            return if (weekMode == ScheduleWeekMode.Current) {
                courses.filter { it.isInWeek(currentWeek) }
            } else {
                courses
            }
        }

    val isEmpty: Boolean
        get() = !loading && visibleCourses.isEmpty()

    val otherCourses: List<OtherScheduleCourse>
        get() = schedule?.otherCourses.orEmpty()
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
            fetchSchedule()
        }
    }

    fun syncUserDataAndReload() {
        if (_uiState.value.loading) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, statusMessage = null, errorMessage = null) }
            authRepository.refreshUserData()
                .onSuccess { message -> fetchSchedule(statusMessage = message) }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            errorMessage = throwable.message ?: "同步数据失败"
                        )
                    }
                }
        }
    }

    private suspend fun fetchSchedule(statusMessage: String? = null) {
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        scheduleRepository.getSchedule()
            .onSuccess { schedule ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        schedule = schedule,
                        statusMessage = statusMessage,
                        errorMessage = if (schedule.courses.isEmpty() && schedule.otherCourses.isEmpty()) "暂无课表/正在同步" else null
                    )
                }
            }
            .onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        statusMessage = statusMessage,
                        errorMessage = throwable.message ?: "暂无课表/正在同步"
                    )
                }
            }
    }

    fun setWeekMode(mode: ScheduleWeekMode) {
        _uiState.update { it.copy(weekMode = mode) }
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

private fun calculateCurrentWeek(now: LocalDate = LocalDate.now()): Int {
    val termStartMonth = if (now.monthValue in 3..8) 3 else 9
    val startYear = if (termStartMonth == 9 && now.monthValue < 3) now.year - 1 else now.year
    val termStart = LocalDate.of(startYear, termStartMonth, 1)
    val diffDays = ChronoUnit.DAYS.between(termStart, now).coerceAtLeast(0)
    return (diffDays / 7 + 1).toInt().coerceIn(1, 20)
}
