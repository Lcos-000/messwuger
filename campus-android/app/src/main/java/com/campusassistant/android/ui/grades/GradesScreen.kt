package com.campusassistant.android.ui.grades

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.campusassistant.android.data.model.GradeItem
import com.campusassistant.android.ui.common.CampusButton
import com.campusassistant.android.ui.common.CampusCard
import com.campusassistant.android.ui.common.CampusEmptyState
import com.campusassistant.android.ui.common.CampusLoadingState
import com.campusassistant.android.ui.common.CampusMessage
import com.campusassistant.android.ui.common.CampusOutlinedButton
import com.campusassistant.android.ui.common.CampusPagePadding
import com.campusassistant.android.ui.common.CampusPageHeader
import com.campusassistant.android.ui.text.LocalAppText

private val SemesterOptions = listOf(
    SemesterOption("3", "秋季/上"),
    SemesterOption("6", "小学期"),
    SemesterOption("12", "春季/下")
)

@Composable
fun GradesScreen(
    state: GradesUiState,
    onAcademicYearChange: (String) -> Unit,
    onYearIncrease: () -> Unit,
    onYearDecrease: () -> Unit,
    onSemesterChange: (String) -> Unit,
    onSortModeChange: (GradeSortMode) -> Unit,
    onViewModeChange: (GradeViewMode) -> Unit,
    onQueryGrades: () -> Unit,
    onSubmitGradeTask: () -> Unit
) {
    val text = LocalAppText.current.grades
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(CampusPagePadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CampusPageHeader(
                title = text.title,
                subtitle = text.subtitle
            )
        }

        item {
            GradeFilterPanel(
                state = state,
                onAcademicYearChange = onAcademicYearChange,
                onYearIncrease = onYearIncrease,
                onYearDecrease = onYearDecrease,
                onSemesterChange = onSemesterChange,
                onSortModeChange = onSortModeChange,
                onViewModeChange = onViewModeChange,
                onQueryGrades = onQueryGrades,
                onSubmitGradeTask = onSubmitGradeTask
            )
        }

        if (!state.statusMessage.isNullOrBlank()) {
            item { CampusMessage(state.statusMessage, isError = false) }
        }
        if (!state.errorMessage.isNullOrBlank()) {
            item { CampusMessage(state.errorMessage, isError = true) }
        }

        when {
            state.loading -> item { CampusLoadingState(text.loading, modifier = Modifier.height(260.dp)) }
            state.isEmptyResult -> item { CampusEmptyState(text.empty, modifier = Modifier.height(260.dp)) }
            state.viewMode == GradeViewMode.Table -> item { GradeTable(grades = state.visibleGrades) }
            else -> items(state.visibleGrades) { grade -> GradeCard(grade) }
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
    onViewModeChange: (GradeViewMode) -> Unit,
    onQueryGrades: () -> Unit,
    onSubmitGradeTask: () -> Unit
) {
    val text = LocalAppText.current.grades
    CampusCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterGroup(title = text.academicYear) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    YearStepButton(
                        text = "-",
                        onClick = onYearDecrease,
                        enabled = !state.loading && !state.submittingTask
                    )
                    OutlinedTextField(
                        value = state.academicYear,
                        onValueChange = onAcademicYearChange,
                        placeholder = { Text("2025") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp)
                    )
                    YearStepButton(
                        text = "+",
                        onClick = onYearIncrease,
                        enabled = !state.loading && !state.submittingTask
                    )
                }
            }

            FilterGroup(title = text.semesterFilter) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SemesterOptions.forEach { option ->
                        ActiveOptionChip(
                            label = option.label,
                            selected = state.semester == option.value,
                            onClick = { onSemesterChange(option.value) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            FilterGroup(title = text.sortFilter) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActiveOptionChip(
                        label = text.defaultSortShort,
                        selected = state.sortMode == GradeSortMode.Default,
                        onClick = { onSortModeChange(GradeSortMode.Default) },
                        modifier = Modifier.weight(1f)
                    )
                    ActiveOptionChip(
                        label = text.scoreDescShort,
                        selected = state.sortMode == GradeSortMode.ScoreDesc,
                        onClick = { onSortModeChange(GradeSortMode.ScoreDesc) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            FilterGroup(title = text.viewFilter) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActiveOptionChip(
                        label = text.cardView,
                        selected = state.viewMode == GradeViewMode.Card,
                        onClick = { onViewModeChange(GradeViewMode.Card) },
                        modifier = Modifier.weight(1f)
                    )
                    ActiveOptionChip(
                        label = text.tableView,
                        selected = state.viewMode == GradeViewMode.Table,
                        onClick = { onViewModeChange(GradeViewMode.Table) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampusButton(
                    text = if (state.loading) text.querying else text.query,
                    onClick = onQueryGrades,
                    modifier = Modifier.weight(1f),
                    enabled = !state.loading && !state.submittingTask
                )
                CampusOutlinedButton(
                    text = if (state.submittingTask) text.submitting else text.latest,
                    onClick = onSubmitGradeTask,
                    modifier = Modifier.weight(1f),
                    enabled = !state.loading && !state.submittingTask
                )
            }
        }
    }
}

@Composable
private fun FilterGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium
        )
        content()
    }
}

@Composable
private fun YearStepButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(
                if (enabled) Color.White.copy(alpha = 0.74f) else Color(0xFFE8EDF5).copy(alpha = 0.62f),
                shape
            )
            .border(1.dp, Color(0xFFD6DEE9), shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color(0xFF18365E) else Color(0xFF94A3B8),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun ActiveOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = modifier
            .shadow(if (selected) 4.dp else 0.dp, shape, clip = false)
            .clip(shape)
            .background(if (selected) Color(0xFF18365E) else Color.White.copy(alpha = 0.72f), shape)
            .border(1.dp, if (selected) Color(0xFF18365E) else Color(0xFFD6DEE9), shape)
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 36.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF334155),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GradeTable(grades: List<GradeItem>) {
    val text = LocalAppText.current.grades
    val columns = listOf(
        GradeTableColumn(text.course, 150.dp) { it.courseName },
        GradeTableColumn(text.score, 74.dp) { it.score },
        GradeTableColumn(text.credit, 70.dp) { it.credit },
        GradeTableColumn(text.gpa, 70.dp) { it.gpa },
        GradeTableColumn(text.teacher, 96.dp) { it.teacher },
        GradeTableColumn(text.nature, 92.dp) { it.courseNature },
        GradeTableColumn(text.type, 104.dp) { it.courseType },
        GradeTableColumn(text.exam, 96.dp) { it.examNature },
        GradeTableColumn(text.sync, 102.dp) { it.syncTime?.take(10) }
    )
    val horizontalScrollState = rememberScrollState()

    CampusCard {
        Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
            GradeTableRow(columns = columns, grade = null, isHeader = true)
            grades.forEachIndexed { index, grade ->
                GradeTableRow(
                    columns = columns,
                    grade = grade,
                    isHeader = false,
                    zebra = index % 2 == 1
                )
            }
        }
    }
}

private data class GradeTableColumn(
    val title: String,
    val width: Dp,
    val value: (GradeItem) -> String?
)

@Composable
private fun GradeTableRow(
    columns: List<GradeTableColumn>,
    grade: GradeItem?,
    isHeader: Boolean,
    zebra: Boolean = false
) {
    Row(
        modifier = Modifier
            .background(
                when {
                    isHeader -> Color(0xFFEAF1FF)
                    zebra -> Color.White.copy(alpha = 0.5f)
                    else -> Color.Transparent
                },
                RoundedCornerShape(if (isHeader) 12.dp else 0.dp)
            )
            .padding(vertical = if (isHeader) 10.dp else 9.dp)
    ) {
        columns.forEach { column ->
            val value = if (isHeader) column.title else grade?.let { column.value(it) }.orDash()
            GradeTableCell(text = value, width = column.width, isHeader = isHeader)
        }
    }
}

@Composable
private fun GradeTableCell(
    text: String,
    width: Dp,
    isHeader: Boolean
) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 9.dp),
        color = if (isHeader) Color(0xFF18365E) else Color(0xFF334155),
        fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Medium,
        style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

private fun String?.orDash(): String = this?.takeIf { it.isNotBlank() } ?: "-"

@Composable
private fun GradeCard(grade: GradeItem) {
    val text = LocalAppText.current.grades
    CampusCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = grade.courseName?.takeIf { it.isNotBlank() } ?: text.unnamedCourse,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOfNotNull(
                            grade.courseCode?.takeIf { it.isNotBlank() },
                            grade.courseNature?.takeIf { it.isNotBlank() },
                            grade.courseType?.takeIf { it.isNotBlank() }
                        ).joinToString(" · ").ifBlank { text.courseInfoMissing },
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                ScoreBadge(score = grade.score)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GradeMeta(text.credit, grade.credit)
                GradeMeta(text.gpa, grade.gpa)
                GradeMeta(text.teacher, grade.teacher)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GradeMeta(text.exam, grade.examNature)
                GradeMeta(text.semester, grade.semester)
                GradeMeta(text.sync, grade.syncTime?.take(10))
            }
        }
    }
}

@Composable
private fun ScoreBadge(score: String?) {
    Box(
        modifier = Modifier
            .background(Color(0xFFEAF1FF), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = score?.takeIf { it.isNotBlank() } ?: "-",
            color = Color(0xFF18365E),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )
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
