package com.campusassistant.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.campusassistant.android.data.model.UserPersonal
import com.campusassistant.android.data.model.UserStatus

@Composable
internal fun InfoRows(personal: UserPersonal?, tokenExists: Boolean) {
    ProfileRow(Icons.Default.Badge, Color(0xFFEFF6FF), HeroStart, "学号", personal?.studentId ?: if (tokenExists) "已登录" else "未登录")
    ProfileRow(Icons.Default.Apartment, Color(0xFFF0FDF4), Color(0xFF16A34A), "学院", personal?.college)
    ProfileRow(Icons.Default.School, Color(0xFFFFF7ED), Color(0xFFEA580C), "专业", personal?.major)
    ProfileRow(Icons.Default.Groups, Color(0xFFF5F3FF), Color(0xFF7C3AED), "班级", personal?.className)
}

@Composable
internal fun StatusRows(
    status: UserStatus?,
    loading: Boolean,
    updatingAutoPunch: Boolean,
    autoPunchEnabled: Boolean,
    onAutoPunchChange: (Boolean) -> Unit
) {
    ProfileRow(Icons.Default.Cached, syncStatusColor(status?.syncStatus).copy(alpha = 0.12f), syncStatusColor(status?.syncStatus), "同步状态", syncStatusText(status?.syncStatus))
    ProfileRow(Icons.Default.Schedule, punchStatusColor(status?.punchStatus).copy(alpha = 0.12f), punchStatusColor(status?.punchStatus), "打卡状态", punchStatusText(status?.punchStatus))
    AutoPunchRow(autoPunchEnabled, loading, updatingAutoPunch, status != null, onAutoPunchChange)
}

@Composable
private fun AutoPunchRow(
    enabled: Boolean,
    loading: Boolean,
    updating: Boolean,
    hasStatus: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        RowIcon(Icons.Default.Shield, Color(0xFFEFF6FF), HeroStart)
        Column(modifier = Modifier.weight(1f)) {
            Text("自动打卡", color = TextMuted, style = MaterialTheme.typography.labelMedium)
            Text(
                if (updating) "正在更新" else if (enabled) "已开启" else "已关闭",
                color = TextStrong,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Switch(checked = enabled, onCheckedChange = onCheckedChange, enabled = hasStatus && !loading && !updating)
    }
}

@Composable
internal fun MessageRows(profileState: ProfileUiState) {
    val error = profileState.errorMessage
    val action = profileState.actionMessage
    if (error.isNullOrBlank() && action.isNullOrBlank() && !profileState.loading) return

    SectionDivider()
    if (profileState.loading) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Text("正在刷新个人主页", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
    if (!error.isNullOrBlank()) {
        Text(error, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
    if (!action.isNullOrBlank()) {
        Text(action, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
internal fun ProfileRow(icon: ImageVector, iconBackground: Color, iconColor: Color, label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        RowIcon(icon, iconBackground, iconColor)
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelMedium)
            Text(
                value?.takeIf { it.isNotBlank() } ?: "-",
                color = TextStrong,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun ActionRow(icon: ImageVector, iconBackground: Color, iconColor: Color, label: String, value: String?, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        ProfileRow(icon, iconBackground, iconColor, label, value ?: "")
    }
}

@Composable
internal fun RowIcon(icon: ImageVector, background: Color, tint: Color) {
    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(background), contentAlignment = Alignment.Center) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
    }
}

@Composable
internal fun SectionDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = Color(0xFFE8EEF7))
}

private fun syncStatusText(value: Int?): String = when (value) {
    0 -> "未同步"
    1 -> "已同步"
    2 -> "同步中"
    3 -> "同步失败"
    null -> "-"
    else -> "状态 $value"
}

private fun punchStatusText(value: Int?): String = when (value) {
    0 -> "未打卡"
    1 -> "已打卡"
    2 -> "打卡中"
    3 -> "打卡失败"
    null -> "-"
    else -> "状态 $value"
}

private fun syncStatusColor(value: Int?): Color = when (value) {
    1 -> Color(0xFF10B981)
    2 -> HeroStart
    3 -> Color(0xFFEF4444)
    else -> Color(0xFF94A3B8)
}

private fun punchStatusColor(value: Int?): Color = when (value) {
    1 -> Color(0xFF10B981)
    2 -> HeroStart
    3 -> Color(0xFFEF4444)
    else -> Color(0xFF94A3B8)
}
