package com.campusassistant.android.ui.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileRowsTest {

    @Test
    fun syncStatusText_usesBackendStatusMapping() {
        assertEquals("未同步", syncStatusText(0))
        assertEquals("同步中", syncStatusText(1))
        assertEquals("已同步", syncStatusText(2))
        assertEquals("同步失败", syncStatusText(3))
        assertEquals("-", syncStatusText(null))
    }

    @Test
    fun punchStatusText_usesBackendStatusMapping() {
        assertEquals("未打卡", punchStatusText(0))
        assertEquals("打卡中", punchStatusText(1))
        assertEquals("已打卡", punchStatusText(2))
        assertEquals("打卡失败", punchStatusText(3))
    }
}
