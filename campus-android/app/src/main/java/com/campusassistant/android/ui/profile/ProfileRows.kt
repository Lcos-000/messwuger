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
import com.campusassistant.android.ui.text.LocalAppText

@Composable
internal fun InfoRows(personal: UserPersonal?, tokenExists: Boolean) {
    val text = LocalAppText.current.profile
    ProfileRow(Icons.Default.Badge, Color(0xFFEFF6FF), HeroStart, text.studentId, personal?.studentId ?: if (tokenExists) text.loggedIn else text.loggedOut)
    ProfileRow(Icons.Default.Apartment, Color(0xFFF0FDF4), Color(0xFF16A34A), text.college, personal?.college)
    ProfileRow(Icons.Default.School, Color(0xFFFFF7ED), Color(0xFFEA580C), text.major, personal?.major)
    ProfileRow(Icons.Default.Groups, Color(0xFFF5F3FF), Color(0xFF7C3AED), text.className, personal?.className)
}

@Composable
internal fun StatusRows(
    status: UserStatus?,
    loading: Boolean,
    updatingAutoPunch: Boolean,
    autoPunchEnabled: Boolean,
    onAutoPunchChange: (Boolean) -> Unit
) {
    val text = LocalAppText.current.profile
    ProfileRow(Icons.Default.Cached, statusColor(status?.syncStatus).copy(alpha = 0.12f), statusColor(status?.syncStatus), text.syncStatus, statusText(status?.syncStatus, SyncLabels))
    ProfileRow(Icons.Default.Schedule, statusColor(status?.punchStatus).copy(alpha = 0.12f), statusColor(status?.punchStatus), text.punchStatus, statusText(status?.punchStatus, PunchLabels))
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
    val text = LocalAppText.current.profile
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        RowIcon(Icons.Default.Shield, Color(0xFFEFF6FF), HeroStart)
        Column(modifier = Modifier.weight(1f)) {
            Text(text.autoPunch, color = TextMuted, style = MaterialTheme.typography.labelMedium)
            Text(
                if (updating) text.updating else if (enabled) text.enabled else text.disabled,
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
    val text = LocalAppText.current.profile
    val error = profileState.errorMessage
    val action = profileState.actionMessage
    if (error.isNullOrBlank() && action.isNullOrBlank() && !profileState.loading && !profileState.deletingAccount) return

    SectionDivider()
    if (profileState.loading) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Text(text.refreshingProfile, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
    if (profileState.deletingAccount) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Text(text.deletingAccount, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
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

private data class StatusLabels(val idle: String, val done: String, val running: String, val failed: String)

private val SyncLabels = StatusLabels("未同步", "已同步", "同步中", "同步失败")
private val PunchLabels = StatusLabels("未打卡", "已打卡", "打卡中", "打卡失败")

private fun statusText(value: Int?, labels: StatusLabels): String = when (value) {
    0 -> labels.idle
    1 -> labels.running
    2 -> labels.done
    3 -> labels.failed
    null -> "-"
    else -> "状态 $value"
}

private fun statusColor(value: Int?): Color = when (value) {
    1 -> Color(0xFF10B981)
    2 -> HeroStart
    3 -> Color(0xFFEF4444)
    else -> Color(0xFF94A3B8)
}
