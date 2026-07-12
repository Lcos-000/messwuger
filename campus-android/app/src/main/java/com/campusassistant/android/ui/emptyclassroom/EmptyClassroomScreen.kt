package com.campusassistant.android.ui.emptyclassroom

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
import androidx.compose.foundation.layout.size
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
import com.campusassistant.android.data.model.EmptyClassroomItem
import com.campusassistant.android.ui.common.CampusButton
import com.campusassistant.android.ui.common.CampusCard
import com.campusassistant.android.ui.common.CampusEmptyState
import com.campusassistant.android.ui.common.CampusLoadingState
import com.campusassistant.android.ui.common.CampusMessage
import com.campusassistant.android.ui.common.CampusOutlinedButton
import com.campusassistant.android.ui.common.CampusPage
import com.campusassistant.android.ui.common.CampusPageHeader

@Composable
fun EmptyClassroomScreen(
    state: EmptyClassroomUiState,
    onAcademicYearChange: (String) -> Unit,
    onYearIncrease: () -> Unit,
    onYearDecrease: () -> Unit,
    onSemesterChange: (String) -> Unit,
    onDayChange: (String) -> Unit,
    onWeekToggle: (Int) -> Unit,
    onPeriodToggle: (Int) -> Unit,
    onCampusChange: (String) -> Unit,
    onBuildingChange: (String) -> Unit,
    onSubmitTask: () -> Unit,
    onQueryResult: () -> Unit,
    onReset: () -> Unit
) {
    CampusPage {
        CampusPageHeader(
            title = "空教室",
            subtitle = "提交查询任务后，再查询空教室结果"
        )

        Spacer(modifier = Modifier.height(14.dp))
        EmptyClassroomFilterPanel(
            state = state,
            onAcademicYearChange = onAcademicYearChange,
            onYearIncrease = onYearIncrease,
            onYearDecrease = onYearDecrease,
            onSemesterChange = onSemesterChange,
            onDayChange = onDayChange,
            onWeekToggle = onWeekToggle,
            onPeriodToggle = onPeriodToggle,
            onCampusChange = onCampusChange,
            onBuildingChange = onBuildingChange,
            onSubmitTask = onSubmitTask,
            onQueryResult = onQueryResult,
            onReset = onReset
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
            state.loadingResult -> CampusLoadingState("正在查询空教室")
            state.resultReady && state.classrooms.isEmpty() -> CampusEmptyState("暂无空教室结果")
            else -> ClassroomList(classrooms = state.classrooms)
        }
    }
}

@Composable
private fun EmptyClassroomFilterPanel(
    state: EmptyClassroomUiState,
    onAcademicYearChange: (String) -> Unit,
    onYearIncrease: () -> Unit,
    onYearDecrease: () -> Unit,
    onSemesterChange: (String) -> Unit,
    onDayChange: (String) -> Unit,
    onWeekToggle: (Int) -> Unit,
    onPeriodToggle: (Int) -> Unit,
    onCampusChange: (String) -> Unit,
    onBuildingChange: (String) -> Unit,
    onSubmitTask: () -> Unit,
    onQueryResult: () -> Unit,
    onReset: () -> Unit
) {
    val busy = state.loadingTask || state.loadingResult
    CampusCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CampusOutlinedButton(text = "-", onClick = onYearDecrease, modifier = Modifier.weight(0.28f), enabled = !busy)
                OutlinedTextField(
                    value = state.academicYear,
                    onValueChange = onAcademicYearChange,
                    label = { Text("学年") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                CampusOutlinedButton(text = "+", onClick = onYearIncrease, modifier = Modifier.weight(0.28f), enabled = !busy)
            }

            OptionRow("学期", state.semesterOptions, state.semester, onSemesterChange)
            OptionRow("星期", state.dayOptions, state.dayOfWeek, onDayChange)
            MultiSelectNumberRow("周次", 1..20, state.selectedWeeks, onWeekToggle)
            MultiSelectNumberRow("节次", 1..14, state.selectedPeriods, onPeriodToggle)
            OptionRow("校区", state.campusOptions, state.campusId, onCampusChange)
            OptionRow("楼栋", state.buildingOptions, state.building, onBuildingChange)

            Text(
                text = state.requestPreview,
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodySmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampusButton(
                    text = if (state.loadingTask) "提交中" else "提交任务",
                    onClick = onSubmitTask,
                    modifier = Modifier.weight(1f),
                    enabled = !busy
                )
                CampusOutlinedButton(
                    text = if (state.loadingResult) "查询中" else "查询结果",
                    onClick = onQueryResult,
                    modifier = Modifier.weight(1f),
                    enabled = !busy
                )
            }
            CampusOutlinedButton(text = "重置选择", onClick = onReset, enabled = !busy)
        }
    }
}

@Composable
private fun OptionRow(
    label: String,
    options: List<OptionItem>,
    selectedValue: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                AssistChip(
                    onClick = { onSelect(option.value) },
                    label = { Text(option.label) },
                    leadingIcon = if (selectedValue == option.value) ({ ModeDot() }) else null
                )
            }
        }
    }
}

@Composable
private fun MultiSelectNumberRow(
    label: String,
    range: IntRange,
    selectedValues: Set<Int>,
    onToggle: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "$label：${selectedValues.sorted().joinToString("、")}",
            color = Color(0xFF64748B),
            style = MaterialTheme.typography.bodySmall
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            range.forEach { value ->
                AssistChip(
                    onClick = { onToggle(value) },
                    label = { Text(value.toString()) },
                    leadingIcon = if (selectedValues.contains(value)) ({ ModeDot() }) else null
                )
            }
        }
    }
}

@Composable
private fun ModeDot() {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(Color(0xFF4F86F7), RoundedCornerShape(50))
    )
}

@Composable
private fun ClassroomList(classrooms: List<EmptyClassroomItem>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(classrooms) { classroom ->
            ClassroomCard(classroom)
        }
    }
}

@Composable
private fun ClassroomCard(classroom: EmptyClassroomItem) {
    CampusCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = classroom.roomName?.takeIf { it.isNotBlank() } ?: classroom.roomCode ?: "未命名教室",
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOfNotNull(
                            classroom.building?.takeIf { it.isNotBlank() },
                            classroom.campus?.takeIf { it.isNotBlank() },
                            classroom.roomType?.takeIf { it.isNotBlank() }
                        ).joinToString(" · ").ifBlank { "教室信息未完善" },
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = classroom.floor?.takeIf { it.isNotBlank() }?.let { "${it}F" } ?: "-",
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Bold
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ClassroomMeta("代码", classroom.roomCode)
                ClassroomMeta("容量", classroom.capacity)
                ClassroomMeta("实际容量", classroom.realCapacity)
            }
            if (!classroom.remark.isNullOrBlank()) {
                Text(
                    text = classroom.remark,
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ClassroomMeta(label: String, value: String?) {
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
