package com.campusassistant.android.ui.emptyclassroom

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.campusassistant.android.ui.common.CampusOutlinedButton
import com.campusassistant.android.ui.common.CampusPageHeader
import com.campusassistant.android.ui.common.CampusSectionTitle
import com.campusassistant.android.ui.common.CampusStatusMessages
import com.campusassistant.android.ui.common.CampusTextField
import com.campusassistant.android.ui.common.ModeDot
import com.campusassistant.android.ui.text.LocalAppText

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
    val text = LocalAppText.current.emptyClassroom
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        state = listState,
        contentPadding = PaddingValues(top = 18.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CampusPageHeader(
                title = text.title,
                subtitle = text.subtitle
            )
        }
        item {
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
        }
        item {
            CampusStatusMessages(
                statusMessage = state.statusMessage,
                errorMessage = state.errorMessage
            )
        }
        if (state.loadingResult || (state.resultReady && state.classrooms.isEmpty())) {
            item {
                Crossfade(targetState = state.loadingResult, label = "classroom-status") { isLoading ->
                    if (isLoading) {
                        CampusLoadingState(text.loading, modifier = Modifier.height(220.dp))
                    } else {
                        CampusEmptyState(text.empty, modifier = Modifier.height(220.dp))
                    }
                }
            }
        } else {
            items(state.classrooms) { classroom -> ClassroomCard(classroom) }
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
    val text = LocalAppText.current.emptyClassroom
    CampusCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                YearStepButton(text = "-", enabled = !busy, onClick = onYearDecrease)
                CampusTextField(
                    value = state.academicYear,
                    onValueChange = onAcademicYearChange,
                    label = text.academicYear,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                YearStepButton(text = "+", enabled = !busy, onClick = onYearIncrease)
            }

            OptionRow(text.semester, state.semesterOptions, state.semester, onSemesterChange)
            OptionRow(text.weekday, state.dayOptions, state.dayOfWeek, onDayChange)
            MultiSelectNumberRow(text.weeks, 1..20, state.selectedWeeks, onWeekToggle)
            MultiSelectNumberRow(text.periods, 1..14, state.selectedPeriods, onPeriodToggle)
            OptionRow(text.campus, state.campusOptions, state.campusId, onCampusChange)
            OptionRow(text.building, state.buildingOptions, state.building, onBuildingChange)

            Text(
                text = state.requestPreview,
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodySmall
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampusButton(
                    text = text.submitTask,
                    onClick = onSubmitTask,
                    modifier = Modifier.weight(1f),
                    enabled = !busy,
                    loading = state.loadingTask
                )
                CampusOutlinedButton(
                    text = if (state.loadingResult) text.querying else text.queryResult,
                    onClick = onQueryResult,
                    modifier = Modifier.weight(1f),
                    enabled = !busy
                )
            }
            CampusOutlinedButton(text = text.reset, onClick = onReset, enabled = !busy)
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
        CampusSectionTitle(title = label)
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
        CampusSectionTitle(title = "$label：${selectedValues.sorted().joinToString("、")}")
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
private fun ClassroomCard(classroom: EmptyClassroomItem) {
    val text = LocalAppText.current.emptyClassroom
    CampusCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = classroom.roomName?.takeIf { it.isNotBlank() } ?: classroom.roomCode ?: text.unnamedRoom,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOfNotNull(
                            classroom.building?.takeIf { it.isNotBlank() },
                            classroom.campus?.takeIf { it.isNotBlank() },
                            classroom.roomType?.takeIf { it.isNotBlank() }
                        ).joinToString(" · ").ifBlank { text.roomInfoMissing },
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                FloorBadge(floor = classroom.floor)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ClassroomMeta(text.code, classroom.roomCode)
                ClassroomMeta(text.capacity, classroom.capacity)
                ClassroomMeta(text.realCapacity, classroom.realCapacity)
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
private fun FloorBadge(floor: String?) {
    Box(
        modifier = Modifier
            .background(Color(0xFFEAF1FF), RoundedCornerShape(12.dp))
            .padding(horizontal = 11.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = floor?.takeIf { it.isNotBlank() }?.let { "${it}F" } ?: "-",
            color = Color(0xFF18365E),
            fontWeight = FontWeight.Bold
        )
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

@Composable
private fun YearStepButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
            .background(
                if (enabled) Color.White.copy(alpha = 0.74f) else Color(0xFFE8EDF5).copy(alpha = 0.62f),
                shape
            )
            .border(1.dp, Color(0xFFD6DEE9), shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (text == "+") Icons.Filled.Add else Icons.Filled.Remove,
            contentDescription = text,
            tint = if (enabled) Color(0xFF18365E) else Color(0xFF94A3B8),
            modifier = Modifier.size(24.dp)
        )
    }
}
