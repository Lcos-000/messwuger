package com.campusassistant.android.ui.grades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.campusassistant.android.data.model.GradeItem
import com.campusassistant.android.data.repository.GradesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class GradeSortMode {
    Default,
    ScoreDesc
}

enum class GradeViewMode {
    Card,
    Table
}

data class SemesterOption(
    val value: String,
    val label: String
)

data class GradesUiState(
    val academicYear: String = LocalDate.now().year.toString(),
    val semester: String = "12",
    val sortMode: GradeSortMode = GradeSortMode.Default,
    val viewMode: GradeViewMode = GradeViewMode.Card,
    val grades: List<GradeItem> = emptyList(),
    val queried: Boolean = false,
    val loading: Boolean = false,
    val submittingTask: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    val visibleGrades: List<GradeItem>
        get() = when (sortMode) {
            GradeSortMode.Default -> grades
            GradeSortMode.ScoreDesc -> grades.sortedByDescending { it.score.toScoreValue() }
        }

    val isEmptyResult: Boolean
        get() = queried && !loading && grades.isEmpty() && errorMessage.isNullOrBlank()
}

class GradesViewModel(
    private val gradesRepository: GradesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GradesUiState())
    val uiState: StateFlow<GradesUiState> = _uiState.asStateFlow()

    fun reset() {
        _uiState.value = GradesUiState()
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

    fun setSortMode(mode: GradeSortMode) {
        _uiState.update { it.copy(sortMode = mode) }
    }

    fun setViewMode(mode: GradeViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun queryGrades() {
        if (_uiState.value.loading || _uiState.value.submittingTask) return
        val state = _uiState.value
        if (state.academicYear.length != 4) {
            _uiState.update { it.copy(errorMessage = "请输入 4 位学年") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, queried = true, statusMessage = null, errorMessage = null) }
            gradesRepository.getGrades(state.academicYear, state.semester)
                .onSuccess { grades ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            grades = grades,
                            errorMessage = null,
                            statusMessage = if (grades.isEmpty()) null else "已查询到 ${grades.size} 条成绩"
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            errorMessage = throwable.message ?: "查询成绩失败"
                        )
                    }
                }
        }
    }

    fun submitLatestTask() {
        if (_uiState.value.loading || _uiState.value.submittingTask) return
        val state = _uiState.value
        if (state.academicYear.length != 4) {
            _uiState.update { it.copy(errorMessage = "请输入 4 位学年") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(submittingTask = true, statusMessage = null, errorMessage = null) }
            gradesRepository.submitGradeTask(state.academicYear, state.semester)
                .onSuccess { message ->
                    _uiState.update {
                        it.copy(
                            submittingTask = false,
                            statusMessage = "$message，请稍后点击查询成绩",
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            submittingTask = false,
                            errorMessage = throwable.message ?: "提交成绩同步任务失败"
                        )
                    }
                }
        }
    }
}

class GradesViewModelFactory(
    private val gradesRepository: GradesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GradesViewModel::class.java)) {
            return GradesViewModel(gradesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun String?.toScoreValue(): Double = this?.toDoubleOrNull() ?: Double.NEGATIVE_INFINITY
