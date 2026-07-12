package com.campusassistant.android.ui.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campusassistant.android.data.model.OtherScheduleCourse
import com.campusassistant.android.data.model.ScheduleCourse

@Composable
internal fun CourseDetailDialog(course: ScheduleCourse, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        },
        title = { Text(course.courseName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (course.teacherSchedules.size <= 1) {
                    DetailLine("教师", course.teacher)
                }
                DetailLine("教室", course.classroom)
                DetailLine("校区", course.campus)
                DetailLine("星期", WeekDays.getOrNull(course.dayOfWeek - 1))
                DetailLine("节次", course.periods ?: "${course.startPeriod}-${course.endPeriod}")
                DetailLine("周次", course.weeks ?: course.weekNumbers.sorted().joinToString(","))
                TeacherScheduleBlock(course)
            }
        }
    )
}

@Composable
private fun TeacherScheduleBlock(course: ScheduleCourse) {
    if (course.teacherSchedules.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "授课安排", color = Color(0xFF64748B))
        course.teacherSchedules.forEach { item ->
            Text(
                text = "${item.teacher?.takeIf { it.isNotBlank() } ?: "-"}：${item.weeks?.takeIf { it.isNotBlank() } ?: "-"}",
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
internal fun OtherCoursesDialog(courses: List<OtherScheduleCourse>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
        title = { Text("其他课程") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (courses.isEmpty()) {
                    Text("暂无其他课程")
                } else {
                    courses.forEach { course ->
                        Column {
                            Text(course.courseName, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = listOfNotNull(
                                    course.teacher?.takeIf { it.isNotBlank() },
                                    course.classroom?.takeIf { it.isNotBlank() },
                                    course.periods?.takeIf { it.isNotBlank() },
                                    course.weeks?.takeIf { it.isNotBlank() },
                                    course.courseType?.takeIf { it.isNotBlank() }
                                ).joinToString(" · ").ifBlank { "信息不完整" },
                                color = Color(0xFF64748B),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun DetailLine(label: String, value: String?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color(0xFF64748B))
        Text(text = value?.takeIf { it.isNotBlank() } ?: "-", fontWeight = FontWeight.Medium)
    }
}
