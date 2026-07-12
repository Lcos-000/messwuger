package com.campusassistant.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(
    val title: String,
    val icon: ImageVector
) {
    Schedule("课表", Icons.Default.CalendarMonth),
    Grades("成绩", Icons.Default.School),
    EmptyClassroom("空教室", Icons.Default.Class),
    Profile("我的", Icons.Default.Person)
}
