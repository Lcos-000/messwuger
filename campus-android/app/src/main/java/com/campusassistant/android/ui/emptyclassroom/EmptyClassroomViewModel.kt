package com.campusassistant.android.ui.emptyclassroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.campusassistant.android.data.model.EmptyClassroomItem
import com.campusassistant.android.data.model.EmptyClassroomRequest
import com.campusassistant.android.data.repository.EmptyClassroomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class OptionItem(
    val value: String,
    val label: String
)

data class EmptyClassroomUiState(
    val academicYear: String = LocalDate.now().year.toString(),
    val semester: String = "12",
    val dayOfWeek: String = "1",
    val selectedWeeks: Set<Int> = setOf(1),
    val selectedPeriods: Set<Int> = setOf(1, 2),
    val campusId: String = "2",
    val building: String = buildingOptionsFor("2").first().value,
    val queryStatus: String? = null,
    val resultReady: Boolean = false,
    val classrooms: List<EmptyClassroomItem> = emptyList(),
    val loadingTask: Boolean = false,
    val loadingResult: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    val weeksMask: String get() = selectedWeeks.toMaskString()
    val periodsMask: String get() = selectedPeriods.toMaskString()
    val campusOptions: List<OptionItem> get() = CampusOptions
    val buildingOptions: List<OptionItem> get() = buildingOptionsFor(campusId)
    val semesterOptions: List<OptionItem> get() = SemesterOptions
    val dayOptions: List<OptionItem> get() = DayOptions
    val requestPreview: String
        get() = "weeksMask=$weeksMask · periodsMask=$periodsMask · building=$building"
}

class EmptyClassroomViewModel(
    private val repository: EmptyClassroomRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EmptyClassroomUiState())
    val uiState: StateFlow<EmptyClassroomUiState> = _uiState.asStateFlow()

    fun reset() {
        _uiState.value = EmptyClassroomUiState()
    }

    fun setAcademicYear(value: String) {
        val normalized = value.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(academicYear = normalized, statusMessage = null, errorMessage = null) }
    }

    fun increaseYear() {
        val next = (_uiState.value.academicYear.toIntOrNull() ?: LocalDate.now().year) + 1
        _uiState.update { it.copy(academicYear = next.toString(), statusMessage = null, errorMessage = null) }
    }

    fun decreaseYear() {
        val next = (_uiState.value.academicYear.toIntOrNull() ?: LocalDate.now().year) - 1
        _uiState.update { it.copy(academicYear = next.toString(), statusMessage = null, errorMessage = null) }
    }

    fun setSemester(value: String) {
        _uiState.update { it.copy(semester = value, statusMessage = null, errorMessage = null) }
    }

    fun setDayOfWeek(value: String) {
        _uiState.update { it.copy(dayOfWeek = value, statusMessage = null, errorMessage = null) }
    }

    fun toggleWeek(week: Int) {
        _uiState.update {
            val next = it.selectedWeeks.toggleKeepingOne(week)
            it.copy(selectedWeeks = next, statusMessage = null, errorMessage = null)
        }
    }

    fun togglePeriod(period: Int) {
        _uiState.update {
            val next = it.selectedPeriods.toggleKeepingOne(period)
            it.copy(selectedPeriods = next, statusMessage = null, errorMessage = null)
        }
    }

    fun setCampus(value: String) {
        val firstBuilding = buildingOptionsFor(value).first().value
        _uiState.update {
            it.copy(
                campusId = value,
                building = firstBuilding,
                statusMessage = null,
                errorMessage = null
            )
        }
    }

    fun setBuilding(value: String) {
        _uiState.update { it.copy(building = value, statusMessage = null, errorMessage = null) }
    }

    fun resetSelection() {
        _uiState.value = EmptyClassroomUiState()
    }

    fun submitTask() {
        if (_uiState.value.loadingTask || _uiState.value.loadingResult) return
        val request = buildRequestOrNull() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loadingTask = true, statusMessage = null, errorMessage = null) }
            repository.submitTask(request)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            loadingTask = false,
                            queryStatus = result.queryStatus,
                            resultReady = result.resultReady == true,
                            statusMessage = statusText(result.queryStatus, result.resultReady == true),
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            loadingTask = false,
                            errorMessage = throwable.message ?: "提交空教室任务失败"
                        )
                    }
                }
        }
    }

    fun queryResult() {
        if (_uiState.value.loadingTask || _uiState.value.loadingResult) return
        val request = buildRequestOrNull() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loadingResult = true, statusMessage = null, errorMessage = null) }
            repository.queryResult(request)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            loadingResult = false,
                            queryStatus = result.queryStatus,
                            resultReady = result.resultReady == true,
                            classrooms = result.classrooms.orEmpty(),
                            statusMessage = statusText(result.queryStatus, result.resultReady == true),
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            loadingResult = false,
                            errorMessage = throwable.message ?: "查询空教室结果失败"
                        )
                    }
                }
        }
    }

    private fun buildRequestOrNull(): EmptyClassroomRequest? {
        val state = _uiState.value
        if (state.academicYear.length != 4) {
            _uiState.update { it.copy(errorMessage = "请输入 4 位学年") }
            return null
        }
        if (state.selectedWeeks.isEmpty() || state.selectedPeriods.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "请至少选择一个周次和一个节次") }
            return null
        }
        return EmptyClassroomRequest(
            academicYear = state.academicYear,
            semester = state.semester,
            dayOfWeek = state.dayOfWeek,
            periodsMask = state.periodsMask,
            weeksMask = state.weeksMask,
            campusId = state.campusId,
            building = state.building,
            roomType = ""
        )
    }
}

class EmptyClassroomViewModelFactory(
    private val repository: EmptyClassroomRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmptyClassroomViewModel::class.java)) {
            return EmptyClassroomViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

val SemesterOptions = listOf(
    OptionItem("3", "学期上 / 秋季"),
    OptionItem("6", "其他 / 小学期"),
    OptionItem("12", "学期下 / 春季")
)

val DayOptions = listOf(
    OptionItem("1", "周一"),
    OptionItem("2", "周二"),
    OptionItem("3", "周三"),
    OptionItem("4", "周四"),
    OptionItem("5", "周五"),
    OptionItem("6", "周六"),
    OptionItem("7", "周日")
)

private val CampusOptions = listOf(
    OptionItem("1", "南区"),
    OptionItem("2", "北区"),
    OptionItem("3", "荣昌校区")
)

fun buildingOptionsFor(campusId: String): List<OptionItem> {
    val values = when (campusId) {
        "1" -> listOf("30", "31", "32", "33", "35", "36", "37", "38", "39", "40", "45", "46", "48", "96", "97", "98")
        "3" -> listOf("RC00", "RC01", "RC02", "RC03", "RC04", "RC05", "RC7B", "RC09")
        else -> listOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "13", "14", "15", "16", "17", "19", "21", "23", "24", "25")
    }
    return values.map { OptionItem(it, it) }
}

private fun Set<Int>.toMaskString(): String = sumOf { 1 shl (it - 1) }.toString()

private fun Set<Int>.toggleKeepingOne(value: Int): Set<Int> {
    return if (contains(value)) {
        if (size == 1) this else this - value
    } else {
        this + value
    }
}

fun statusText(status: String?, ready: Boolean): String {
    return when (status) {
        "RESULT_READY" -> if (ready) "结果已就绪" else "结果状态已就绪，等待返回教室列表"
        "QUERYING" -> "查询中，请稍后再次查询结果"
        "TIMEOUT" -> "等待中或查询超时，可稍后再次查询"
        "SUBMITTED" -> "任务已提交，请稍后点击查询结果"
        "FAILED" -> "查询失败，请调整条件后重试"
        else -> status?.takeIf { it.isNotBlank() } ?: "请求已完成"
    }
}
