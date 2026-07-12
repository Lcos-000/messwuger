package com.campusassistant.android.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.campusassistant.android.data.model.ScheduleCourse
import com.campusassistant.android.ui.common.CampusEmptyState
import com.campusassistant.android.ui.common.CampusLoadingState
import com.campusassistant.android.ui.common.CampusMessage
import com.campusassistant.android.ui.common.CampusOutlinedButton
import com.campusassistant.android.ui.common.CampusPage
import com.campusassistant.android.ui.common.CampusPageHeader
import com.campusassistant.android.ui.common.campusGlassContainerColor

internal val WeekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
private val PeriodTimes = listOf(
    "8:00-8:45", "8:55-9:40", "10:00-10:45", "10:55-11:40",
    "12:10-12:55", "13:05-13:50", "14:00-14:45", "14:55-15:40",
    "15:50-16:35", "16:55-17:40", "17:50-18:35", "19:20-20:05",
    "20:15-21:00", "21:10-21:55"
)
private val CourseColors = listOf(
    Color(0xFF4F86F7),
    Color(0xFF10B981),
    Color(0xFFF59E0B),
    Color(0xFF8B5CF6),
    Color(0xFFEF4444),
    Color(0xFF06B6D4),
    Color(0xFF64748B)
)

@Composable
fun ScheduleScreen(
    state: ScheduleUiState,
    onRefresh: () -> Unit,
    onWeekModeChange: (ScheduleWeekMode) -> Unit,
    onCourseClick: (ScheduleCourse) -> Unit,
    onDismissCourse: () -> Unit,
    onShowOtherCourses: () -> Unit,
    onDismissOtherCourses: () -> Unit
) {
    CampusPage {
        ScheduleHeader(
            state = state,
            onRefresh = onRefresh,
            onWeekModeChange = onWeekModeChange,
            onShowOtherCourses = onShowOtherCourses
        )
        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.loading -> CampusLoadingState("正在加载课表")
            state.isEmpty -> EmptySchedule(message = state.errorMessage ?: "暂无课表/正在同步", onRefresh = onRefresh)
            else -> ScheduleGrid(courses = state.visibleCourses, onCourseClick = onCourseClick)
        }
    }

    state.selectedCourse?.let { course ->
        CourseDetailDialog(course = course, onDismiss = onDismissCourse)
    }

    if (state.showOtherCourses) {
        OtherCoursesDialog(courses = state.otherCourses, onDismiss = onDismissOtherCourses)
    }
}

@Composable
private fun ScheduleHeader(
    state: ScheduleUiState,
    onRefresh: () -> Unit,
    onWeekModeChange: (ScheduleWeekMode) -> Unit,
    onShowOtherCourses: () -> Unit
) {
    Column {
        CampusPageHeader(
            title = "课表",
            subtitle = "当前第 ${state.currentWeek} 周",
            action = {
                CampusOutlinedButton(
                    text = if (state.loading) "同步中" else "同步数据",
                    onClick = onRefresh,
                    modifier = Modifier.width(104.dp),
                    enabled = !state.loading
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = { onWeekModeChange(ScheduleWeekMode.Current) },
                label = { Text("本周") },
                leadingIcon = if (state.weekMode == ScheduleWeekMode.Current) {
                    { ModeDot() }
                } else null
            )
            AssistChip(
                onClick = { onWeekModeChange(ScheduleWeekMode.All) },
                label = { Text("全部周次") },
                leadingIcon = if (state.weekMode == ScheduleWeekMode.All) {
                    { ModeDot() }
                } else null
            )
            if (state.otherCourses.isNotEmpty()) {
                AssistChip(
                    onClick = onShowOtherCourses,
                    label = { Text("其他课程 ${state.otherCourses.size}") }
                )
            }
        }

        state.schedule?.let { schedule ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${schedule.academicYear ?: "-"} 学年 · 学期 ${schedule.semester ?: "-"} · ${state.visibleCourses.size} 门课",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (!state.errorMessage.isNullOrBlank() && !state.isEmpty) {
            Spacer(modifier = Modifier.height(8.dp))
            CampusMessage(state.errorMessage, isError = true)
        }

        if (!state.statusMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            CampusMessage(state.statusMessage, isError = false)
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
private fun EmptySchedule(message: String, onRefresh: () -> Unit) {
    CampusEmptyState(text = message, actionText = "重新请求", onAction = onRefresh)
}

@Composable
private fun ScheduleGrid(
    courses: List<ScheduleCourse>,
    onCourseClick: (ScheduleCourse) -> Unit
) {
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()
    val dayWidth = 104.dp
    val periodWidth = 64.dp
    val rowHeight = 58.dp
    val groupedCourses = courses.groupBy { CourseSlotKey(it.dayOfWeek, it.startPeriod, it.endPeriod) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScroll)
    ) {
        Column(
            modifier = Modifier
                .requiredWidth(periodWidth + dayWidth * 7)
                .verticalScroll(verticalScroll)
        ) {
            Row {
                HeaderCell(text = "节", width = periodWidth)
                WeekDays.forEach { day -> HeaderCell(text = day, width = dayWidth) }
            }

            Row {
                Column(modifier = Modifier.width(periodWidth)) {
                    (1..14).forEach { period ->
                        PeriodCell(
                            period = period,
                            time = PeriodTimes.getOrNull(period - 1).orEmpty(),
                            width = periodWidth,
                            height = rowHeight
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(dayWidth * 7)
                        .height(rowHeight * 14)
                ) {
                    (1..14).forEach { period ->
                        (1..7).forEach { day ->
                            GridCell(
                                modifier = Modifier
                                    .width(dayWidth)
                                    .height(rowHeight)
                                    .offset(x = dayWidth * (day - 1), y = rowHeight * (period - 1)),
                                altRow = period % 2 == 0
                            )
                        }
                    }

                    groupedCourses.forEach { (slot, slotCourses) ->
                        CourseSlot(
                            courses = slotCourses,
                            modifier = Modifier
                                .width(dayWidth)
                                .height(rowHeight * slot.span)
                                .offset(
                                    x = dayWidth * (slot.dayOfWeek - 1),
                                    y = rowHeight * (slot.startPeriod - 1)
                                ),
                            onCourseClick = onCourseClick
                        )
                    }
                }
            }
        }
    }
}


private data class CourseSlotKey(
    val dayOfWeek: Int,
    val startPeriod: Int,
    val endPeriod: Int
) {
    val span: Int = (endPeriod - startPeriod + 1).coerceAtLeast(1)
}

@Composable
private fun GridCell(modifier: Modifier, altRow: Boolean) {
    val base = if (altRow) Color(0xFFFAFCFF) else Color.White
    Box(
        modifier = modifier
            .padding(1.dp)
            .background(
                color = campusGlassContainerColor(base),
                shape = RoundedCornerShape(6.dp)
            )
    )
}

@Composable
private fun CourseSlot(
    courses: List<ScheduleCourse>,
    modifier: Modifier,
    onCourseClick: (ScheduleCourse) -> Unit
) {
    Row(
        modifier = modifier.padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        courses.forEach { course ->
            CourseCard(
                course = course,
                color = CourseColors[course.courseName.hashCode().floorMod(CourseColors.size)],
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                onClick = { onCourseClick(course) }
            )
        }
    }
}


private fun Int.floorMod(modulus: Int): Int = ((this % modulus) + modulus) % modulus

@Composable
private fun HeaderCell(text: String, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(38.dp)
            .padding(3.dp)
            .background(campusGlassContainerColor(Color(0xFFEFF4FB)), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color(0xFF334155), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PeriodCell(period: Int, time: String, width: Dp, height: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .padding(3.dp)
            .background(campusGlassContainerColor(Color(0xFFEFF4FB)), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = period.toString(), color = Color(0xFF334155), fontWeight = FontWeight.SemiBold)
            Text(
                text = time,
                color = Color(0xFF94A3B8),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CourseCard(
    course: ScheduleCourse,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.92f))
    ) {
        Column(modifier = Modifier.padding(7.dp)) {
            Text(
                text = course.courseName,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOfNotNull(course.classroom, course.teacher).joinToString(" · ").ifBlank { course.weeks.orEmpty() },
                color = Color.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = course.weeks?.takeIf { it.isNotBlank() } ?: "第 ${course.startPeriod}-${course.endPeriod} 节",
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
