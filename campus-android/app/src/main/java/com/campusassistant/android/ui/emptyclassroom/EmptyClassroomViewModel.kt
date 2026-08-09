package com.campusassistant.android.ui.emptyclassroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.campusassistant.android.data.model.EmptyClassroomItem
import com.campusassistant.android.data.model.EmptyClassroomRequest
import com.campusassistant.android.data.repository.EmptyClassroomRepository
import com.campusassistant.android.ui.text.AppMessages
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
    val campusId: String = "2",
    val building: String = NorthBuildings.first(),
    val selectedWeeks: Set<Int> = setOf(1),
    val selectedPeriods: Set<Int> = setOf(2, 3),
    val classrooms: List<EmptyClassroomItem> = emptyList(),
    val queryStatus: String? = null,
    val resultReady: Boolean = false,
    val loadingTask: Boolean = false,
    val loadingResult: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    val semesterOptions: List<OptionItem>
        get() = AppMessages.EmptyClassroomOptions.semesters.map { OptionItem(it.first, it.second) }

    val dayOptions: List<OptionItem>
        get() = (1..7).map { OptionItem(it.toString(), AppMessages.EmptyClassroomOptions.weekdays[it - 1]) }

    val campusOptions: List<OptionItem>
        get() = AppMessages.EmptyClassroomOptions.campuses.map { OptionItem(it.first, it.second) }

    val buildingOptions: List<OptionItem>
        get() = buildingsForCampus(campusId).map { OptionItem(it, it) }

    val requestPreview: String
        get() {
            val semesterLabel = semesterOptions.find { it.value == semester }?.label ?: semester
            val dayLabel = dayOptions.find { it.value == dayOfWeek }?.label ?: dayOfWeek
            val campusLabel = campusOptions.find { it.value == campusId }?.label ?: campusId
            val weeks = selectedWeeks.sorted().joinToString("、")
            val periods = selectedPeriods.sorted().joinToString("、")
            return buildString {
                append(semesterLabel)
                append(" · ")
                append(dayLabel)
                if (weeks.isNotBlank()) append(" · 第${weeks}周")
                if (periods.isNotBlank()) append(" · 第${periods}节")
                append(" · ")
                append(campusLabel)
                if (building.isNotBlank()) append(" · $building")
            }
        }
}

class EmptyClassroomViewModel(
    private val repository: EmptyClassroomRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EmptyClassroomUiState())
    val uiState: StateFlow<EmptyClassroomUiState> = _uiState.asStateFlow()

    fun reset() {
        _uiState.value = EmptyClassroomUiState()
    }

    fun resetSelection() = reset()

    fun setAcademicYear(value: String) {
        _uiState.update { it.copy(academicYear = value.filter(Char::isDigit).take(4), errorMessage = null, statusMessage = null) }
    }

    fun increaseYear() {
        val next = (_uiState.value.academicYear.toIntOrNull() ?: LocalDate.now().year) + 1
        _uiState.update { it.copy(academicYear = next.toString(), errorMessage = null, statusMessage = null) }
    }

    fun decreaseYear() {
        val next = (_uiState.value.academicYear.toIntOrNull() ?: LocalDate.now().year) - 1
        _uiState.update { it.copy(academicYear = next.toString(), errorMessage = null, statusMessage = null) }
    }

    fun setSemester(value: String) {
        _uiState.update { it.copy(semester = value, errorMessage = null, statusMessage = null) }
    }

    fun setDayOfWeek(value: String) {
        _uiState.update { it.copy(dayOfWeek = value, errorMessage = null, statusMessage = null) }
    }

    fun toggleWeek(value: Int) {
        _uiState.update { current ->
            current.copy(selectedWeeks = current.selectedWeeks.toggle(value), errorMessage = null, statusMessage = null)
        }
    }

    fun togglePeriod(value: Int) {
        _uiState.update { current ->
            current.copy(selectedPeriods = current.selectedPeriods.toggle(value), errorMessage = null, statusMessage = null)
        }
    }

    fun setCampus(value: String) {
        val firstBuilding = buildingsForCampus(value).firstOrNull().orEmpty()
        _uiState.update {
            it.copy(campusId = value, building = firstBuilding, errorMessage = null, statusMessage = null)
        }
    }

    fun setBuilding(value: String) {
        _uiState.update { it.copy(building = value, errorMessage = null, statusMessage = null) }
    }

    fun submitTask() {
        val request = requestOrNull() ?: return
        if (_uiState.value.loadingTask || _uiState.value.loadingResult) return
        viewModelScope.launch {
            _uiState.update { it.copy(loadingTask = true, errorMessage = null, statusMessage = null) }
            repository.submitTask(request)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            loadingTask = false,
                            queryStatus = response.queryStatus,
                            resultReady = response.resultReady == true,
                            statusMessage = AppMessages.EmptyClassroom.taskStatus(response.queryStatus ?: "SUBMITTED")
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(loadingTask = false, errorMessage = throwable.message ?: AppMessages.EmptyClassroom.submitFailed) }
                }
        }
    }

    fun queryResult() {
        val request = requestOrNull() ?: return
        if (_uiState.value.loadingTask || _uiState.value.loadingResult) return
        viewModelScope.launch {
            _uiState.update { it.copy(loadingResult = true, errorMessage = null, statusMessage = null) }
            repository.queryResult(request)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            loadingResult = false,
                            queryStatus = response.queryStatus,
                            resultReady = response.resultReady == true,
                            classrooms = response.classrooms.orEmpty(),
                            statusMessage = AppMessages.EmptyClassroom.queryStatus(response.queryStatus ?: if (response.resultReady == true) "RESULT_READY" else "QUERYING")
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(loadingResult = false, errorMessage = throwable.message ?: AppMessages.EmptyClassroom.queryFailed) }
                }
        }
    }

    private fun requestOrNull(): EmptyClassroomRequest? {
        val state = _uiState.value
        if (state.academicYear.length != 4) {
            _uiState.update { it.copy(errorMessage = AppMessages.EmptyClassroom.enterYear) }
            return null
        }
        if (state.selectedWeeks.isEmpty()) {
            _uiState.update { it.copy(errorMessage = AppMessages.EmptyClassroom.selectWeek) }
            return null
        }
        if (state.selectedPeriods.isEmpty()) {
            _uiState.update { it.copy(errorMessage = AppMessages.EmptyClassroom.selectPeriod) }
            return null
        }
        return EmptyClassroomRequest(
            academicYear = state.academicYear,
            semester = state.semester,
            dayOfWeek = state.dayOfWeek,
            periodsMask = state.selectedPeriods.toBitMask().toString(),
            weeksMask = state.selectedWeeks.toBitMask().toString(),
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

private fun Set<Int>.toggle(value: Int): Set<Int> = if (contains(value)) this - value else this + value

private fun Set<Int>.toBitMask(): Int = fold(0) { acc, value -> acc + (1 shl (value - 1)) }

private fun buildingsForCampus(campusId: String): List<String> = when (campusId) {
    "1" -> SouthBuildings
    "3" -> RongchangBuildings
    else -> NorthBuildings
}

private val NorthBuildings = listOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "13", "14", "15", "16", "17", "19", "21", "23", "24", "25")
private val SouthBuildings = listOf("30", "31", "32", "33", "35", "36", "37", "38", "39", "40", "45", "46", "48", "96", "97", "98")
private val RongchangBuildings = listOf("RC00", "RC01", "RC02", "RC03", "RC04", "RC05", "RC7B", "RC09")
