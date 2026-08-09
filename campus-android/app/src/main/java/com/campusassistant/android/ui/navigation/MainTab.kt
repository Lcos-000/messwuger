package com.campusassistant.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector
import com.campusassistant.android.ui.text.NavText

enum class MainTab(
    val icon: ImageVector
) {
    Schedule(Icons.Default.CalendarMonth),
    Grades(Icons.Default.School),
    EmptyClassroom(Icons.Default.Class),
    Profile(Icons.Default.Person);

    fun title(text: NavText): String = when (this) {
        Schedule -> text.schedule
        Grades -> text.grades
        EmptyClassroom -> text.emptyClassroom
        Profile -> text.profile
    }
}
