package com.campusassistant.android.data.repository

import com.campusassistant.android.data.model.ScheduleResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleParsingTest {

    @Test
    fun parseSchedule_preservesOddAndEvenWeekSemantics() {
        val schedule = ScheduleResponse(
            scheduleJson = """
                [
                  {"courseName":"高等数学","teacher":"张老师","classroom":"101","dayOfWeek":1,"periods":"1-2","weeks":"1-16周(单)"},
                  {"courseName":"高等数学","teacher":"李老师","classroom":"101","dayOfWeek":1,"periods":"1-2","weeks":"1-16周(双)"}
                ]
            """.trimIndent()
        )

        val course = parseSchedule(schedule).courses.single()

        assertEquals((1..16).toSet(), course.weekNumbers)
        assertEquals("1-16周(单) / 1-16周(双)", course.weeks)
        assertEquals(2, course.teacherSchedules.size)
        assertTrue(course.teacherSchedules.any { it.teacher == "张老师" && it.weeks == "1-16周(单)" })
        assertTrue(course.teacherSchedules.any { it.teacher == "李老师" && it.weeks == "1-16周(双)" })
    }
}
