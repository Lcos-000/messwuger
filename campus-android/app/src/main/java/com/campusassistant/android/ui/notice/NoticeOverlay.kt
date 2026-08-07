package com.campusassistant.android.ui.notice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campusassistant.android.data.model.PublicNotice
import com.campusassistant.android.ui.common.verticalFadeEdges
import com.campusassistant.android.ui.text.LocalAppText

@Composable
fun NoticeOverlay(
    state: NoticeUiState,
    onOpenLatestNotice: () -> Unit,
    onOpenNoticeHistory: () -> Unit,
    onOpenHistoryNotice: (PublicNotice) -> Unit,
    onCloseNoticeDialogs: () -> Unit
) {
    NoticeEntryButton(
        hasUnread = state.hasUnread,
        hasNotice = state.latestNotice?.content?.isNotBlank() == true,
        onClick = onOpenLatestNotice
    )

    if (state.showNoticeDialog && state.activeNotice != null) {
        NoticeDetailDialog(
            notice = state.activeNotice,
            onDismiss = onCloseNoticeDialogs,
            onShowHistory = onOpenNoticeHistory
        )
    }

    if (state.showHistoryDialog) {
        NoticeHistoryDialog(
            notices = state.history,
            onDismiss = onCloseNoticeDialogs,
            onNoticeClick = onOpenHistoryNotice
        )
    }
}

@Composable
private fun NoticeEntryButton(
    hasUnread: Boolean,
    hasNotice: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 72.dp, end = 18.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            IconButton(onClick = onClick, enabled = hasNotice) {
                BadgedBox(
                    badge = {
                        if (hasUnread) {
                            Badge()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = LocalAppText.current.common.notice,
                        tint = if (hasNotice) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.36f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoticeDetailDialog(
    notice: PublicNotice,
    onDismiss: () -> Unit,
    onShowHistory: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = notice.title?.ifBlank { LocalAppText.current.common.notice }
                        ?: LocalAppText.current.common.notice,
                    style = MaterialTheme.typography.titleMedium
                )
                if (!notice.updatedAt.isNullOrBlank()) {
                    Text(
                        text = notice.updatedAt.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!notice.level.isNullOrBlank()) {
                    Text(
                        text = notice.level.orEmpty().uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = notice.content.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onShowHistory) {
                Text(LocalAppText.current.notice.viewHistory)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LocalAppText.current.common.close)
            }
        }
    )
}

@Composable
private fun NoticeHistoryDialog(
    notices: List<PublicNotice>,
    onDismiss: () -> Unit,
    onNoticeClick: (PublicNotice) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LocalAppText.current.notice.historyTitle) },
        text = {
            if (notices.isEmpty()) {
                Text(LocalAppText.current.notice.emptyHistory, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .verticalFadeEdges(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    notices.forEach { notice ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onNoticeClick(notice) },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                                Text(
                                    text = notice.title?.ifBlank { LocalAppText.current.common.notice }
                                        ?: LocalAppText.current.common.notice,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                if (!notice.updatedAt.isNullOrBlank()) {
                                    Text(
                                        text = notice.updatedAt.orEmpty(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Text(
                                    text = notice.content.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(LocalAppText.current.common.close)
            }
        }
    )
}
