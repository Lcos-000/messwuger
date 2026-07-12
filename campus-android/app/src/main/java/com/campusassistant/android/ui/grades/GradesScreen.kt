package com.campusassistant.android.ui.grades

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.campusassistant.android.data.model.GradeItem
import com.campusassistant.android.ui.common.CampusButton
import com.campusassistant.android.ui.common.CampusCard
import com.campusassistant.android.ui.common.CampusEmptyState
import com.campusassistant.android.ui.common.CampusLoadingState
import com.campusassistant.android.ui.common.CampusMessage
import com.campusassistant.android.ui.common.CampusOutlinedButton
import com.campusassistant.android.ui.common.CampusPage
import com.campusassistant.android.ui.common.CampusPageHeader

private val SemesterOptions = listOf(
    SemesterOption("3", "学期上 / 秋季"),
    SemesterOption("6", "其他 / 小学期"),
    SemesterOption("12", "学期下 / 春季")
)

@Composable
fun GradesScreen(
    state: GradesUiState,
    onAcademicYearChange: (String) -> Unit,
    onYearIncrease: () -> Unit,
    onYearDecrease: () -> Unit,
    onSemesterChange: (String) -> Unit,
    onSortModeChange: (GradeSortMode) -> Unit,
    onQueryGrades: () -> Unit,
    onSubmitGradeTask: () -> Unit
) {
    CampusPage {
        CampusPageHeader(
            title = "成绩",
            subtitle = "查询已同步成绩，或提交任务获取最新成绩"
        )

        Spacer(modifier = Modifier.height(14.dp))
        GradeFilterPanel(
            state = state,
            onAcademicYearChange = onAcademicYearChange,
            onYearIncrease = onYearIncrease,
            onYearDecrease = onYearDecrease,
            onSemesterChange = onSemesterChange,
            onSortModeChange = onSortModeChange,
            onQueryGrades = onQueryGrades,
            onSubmitGradeTask = onSubmitGradeTask
        )

        if (!state.statusMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            CampusMessage(state.statusMessage, isError = false)
        }
        if (!state.errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            CampusMessage(state.errorMessage, isError = true)
        }

        Spacer(modifier = Modifier.height(12.dp))
        when {
            state.loading -> CampusLoadingState("正在查询成绩")
            state.isEmptyResult -> CampusEmptyState("暂无成绩数据")
            else -> GradeList(grades = state.visibleGrades)
        }
    }
}

@Composable
private fun GradeFilterPanel(
    state: GradesUiState,
    onAcademicYearChange: (String) -> Unit,
    onYearIncrease: () -> Unit,
    onYearDecrease: () -> Unit,
    onSemesterChange: (String) -> Unit,
    onSortModeChange: (GradeSortMode) -> Unit,
    onQueryGrades: () -> Unit,
    onSubmitGradeTask: () -> Unit
) {
    CampusCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CampusOutlinedButton(text = "-", onClick = onYearDecrease, modifier = Modifier.weight(0.28f), enabled = !state.loading && !state.submittingTask)
                OutlinedTextField(
                    value = state.academicYear,
                    onValueChange = onAcademicYearChange,
                    label = { Text("学年") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                CampusOutlinedButton(text = "+", onClick = onYearIncrease, modifier = Modifier.weight(0.28f), enabled = !state.loading && !state.submittingTask)
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SemesterOptions.forEach { option ->
                    AssistChip(
                        onClick = { onSemesterChange(option.value) },
                        label = { Text(option.label) },
                        leadingIcon = if (state.semester == option.value) ({ ModeDot() }) else null
                    )
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { onSortModeChange(GradeSortMode.Default) },
                    label = { Text("默认排序") },
                    leadingIcon = if (state.sortMode == GradeSortMode.Default) ({ ModeDot() }) else null
                )
                AssistChip(
                    onClick = { onSortModeChange(GradeSortMode.ScoreDesc) },
                    label = { Text("成绩从高到低") },
                    leadingIcon = if (state.sortMode == GradeSortMode.ScoreDesc) ({ ModeDot() }) else null
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampusButton(
                    text = if (state.loading) "查询中" else "查询成绩",
                    onClick = onQueryGrades,
                    modifier = Modifier.weight(1f),
                    enabled = !state.loading && !state.submittingTask
                )
                CampusOutlinedButton(
                    text = if (state.submittingTask) "提交中" else "获取最新成绩",
                    onClick = onSubmitGradeTask,
                    modifier = Modifier.weight(1f),
                    enabled = !state.loading && !state.submittingTask
                )
            }
        }
    }
}

@Composable
private fun ModeDot() {
    Box(
        modifier = Modifier
            .height(8.dp)
            .padding(horizontal = 4.dp)
            .background(Color(0xFF4F86F7), RoundedCornerShape(50))
    )
}

@Composable
private fun GradeList(grades: List<GradeItem>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(grades) { grade ->
            GradeCard(grade)
        }
    }
}

@Composable
private fun GradeCard(grade: GradeItem) {
    CampusCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = grade.courseName?.takeIf { it.isNotBlank() } ?: "未命名课程",
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOfNotNull(
                            grade.courseCode?.takeIf { it.isNotBlank() },
                            grade.courseNature?.takeIf { it.isNotBlank() },
                            grade.courseType?.takeIf { it.isNotBlank() }
                        ).joinToString(" · ").ifBlank { "课程信息未完善" },
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = grade.score?.takeIf { it.isNotBlank() } ?: "-",
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GradeMeta("学分", grade.credit)
                GradeMeta("绩点", grade.gpa)
                GradeMeta("教师", grade.teacher)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GradeMeta("考试", grade.examNature)
                GradeMeta("学期", grade.semester)
                GradeMeta("同步", grade.syncTime?.take(10))
            }
        }
    }
}

@Composable
private fun GradeMeta(label: String, value: String?) {
    Column {
        Text(label, color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelSmall)
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "-",
            color = Color(0xFF334155),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
