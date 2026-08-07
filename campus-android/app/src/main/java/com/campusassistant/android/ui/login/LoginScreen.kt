package com.campusassistant.android.ui.login

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusassistant.android.ui.common.CampusButton
import com.campusassistant.android.ui.common.CampusLoadingState
import com.campusassistant.android.ui.common.CampusMessage
import com.campusassistant.android.ui.common.CampusTextField
import com.campusassistant.android.ui.profile.ServerSettingsDraft
import com.campusassistant.android.ui.text.LocalAppText

@Composable
fun LoginScreen(
    loading: Boolean,
    errorMessage: String?,
    serverSettingsDraft: ServerSettingsDraft,
    savingServerConfig: Boolean,
    onServerSettingsToggle: () -> Unit,
    onServerHostChange: (String) -> Unit,
    onServerPortChange: (String) -> Unit,
    onSaveServerSettings: () -> Unit,
    showBackToApp: Boolean,
    onBackToApp: () -> Unit,
    onLogin: (String, String) -> Unit
) {
    var studentId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LoginBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp, Alignment.CenterVertically)
        ) {
            BrandSection()
            LoginPanel(
                studentId = studentId,
                password = password,
                loading = loading,
                errorMessage = errorMessage,
                serverSettingsDraft = serverSettingsDraft,
                savingServerConfig = savingServerConfig,
                onServerSettingsToggle = onServerSettingsToggle,
                onServerHostChange = onServerHostChange,
                onServerPortChange = onServerPortChange,
                onSaveServerSettings = onSaveServerSettings,
                showBackToApp = showBackToApp,
                onBackToApp = onBackToApp,
                onStudentIdChange = { studentId = it },
                onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                onLogin = { onLogin(studentId, password) }
            )
        }
    }
}

@Composable
private fun LoginBackground() {
    val transition = rememberInfiniteTransition(label = "loginBackground")
    val pulse = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientPulse"
    ).value
    val drift = transition.animateFloat(
        initialValue = -0.08f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientDrift"
    ).value
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF0F1B2E),
                        Color(0xFF152742),
                        Color(0xFF1D365B),
                        Color(0xFF2A4871)
                    )
                )
            )
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val warmCenter = Offset(size.width * (0.16f + drift), size.height * (0.18f + pulse * 0.04f))
        val coolCenter = Offset(size.width * (0.88f - drift), size.height * (0.44f - pulse * 0.04f))
        val baseAlpha = 0.14f + pulse * 0.06f
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF9FC4FF).copy(alpha = baseAlpha),
                    Color(0xFF9FC4FF).copy(alpha = baseAlpha * 0.34f),
                    Color.Transparent
                ),
                center = warmCenter,
                radius = size.minDimension * (0.78f + pulse * 0.08f)
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6C9EEA).copy(alpha = 0.12f + pulse * 0.05f),
                    Color(0xFF6C9EEA).copy(alpha = 0.04f + pulse * 0.02f),
                    Color.Transparent
                ),
                center = coolCenter,
                radius = size.minDimension * (0.88f - pulse * 0.06f)
            )
        )
        val grid = 56.dp.toPx()
        var x = 0f
        while (x <= size.width) {
            drawLine(
                color = Color.White.copy(alpha = 0.035f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += grid
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(
                color = Color.White.copy(alpha = 0.035f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += grid
        }
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.08f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.16f)
                )
            )
        )
    }
}

@Composable
private fun BrandSection() {
    val text = LocalAppText.current.login
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp)
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.08f),
            contentColor = Color.White,
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        ) {
            Text(
                text = text.kicker,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color(0xFFE4EEFF),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(15.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(72.dp)
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFE7F0FF).copy(alpha = 0.92f), Color(0xFFE7F0FF).copy(alpha = 0.16f))
                        ),
                        shape = RoundedCornerShape(999.dp)
                    )
            )
            Column {
                Text(
                    text = text.title,
                    color = Color(0xFFF4F8FF),
                    fontSize = 31.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = text.subtitle,
                    color = Color(0xFFDDE7F6).copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 23.sp
                )
            }
        }
    }
}

@Composable
private fun LoginPanel(
    studentId: String,
    password: String,
    loading: Boolean,
    errorMessage: String?,
    serverSettingsDraft: ServerSettingsDraft,
    savingServerConfig: Boolean,
    onServerSettingsToggle: () -> Unit,
    onServerHostChange: (String) -> Unit,
    onServerPortChange: (String) -> Unit,
    onSaveServerSettings: () -> Unit,
    showBackToApp: Boolean,
    onBackToApp: () -> Unit,
    onStudentIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    onLogin: () -> Unit
) {
    val text = LocalAppText.current.login
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8FBFF).copy(alpha = 0.94f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.34f)),
        shadowElevation = 18.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
            LoginModeTabs()
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = text.studentIdLabel,
                color = Color(0xFF5D6B84),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            CampusTextField(
                value = studentId,
                onValueChange = onStudentIdChange,
                label = text.studentIdPlaceholder
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = text.passwordLabel,
                color = Color(0xFF5D6B84),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(text.passwordPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibilityChange) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                        )
                    }
                }
            )
            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                CampusMessage(errorMessage, isError = true)
            }
            Spacer(modifier = Modifier.height(18.dp))
            CampusButton(
                text = if (loading) text.submitting else text.submit,
                onClick = onLogin,
                enabled = !loading
            )
            Spacer(modifier = Modifier.height(10.dp))
            ServerSettingsEntry(
                draft = serverSettingsDraft,
                saving = savingServerConfig,
                onToggle = onServerSettingsToggle,
                onHostChange = onServerHostChange,
                onPortChange = onServerPortChange,
                showBackToApp = showBackToApp,
                onBackToApp = onBackToApp,
                onSave = onSaveServerSettings
            )
            if (loading) {
                Spacer(modifier = Modifier.height(14.dp))
                CampusLoadingState(text = text.checkingAccount, modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
private fun ServerSettingsEntry(
    draft: ServerSettingsDraft,
    saving: Boolean,
    onToggle: () -> Unit,
    onHostChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    showBackToApp: Boolean,
    onBackToApp: () -> Unit,
    onSave: () -> Unit
) {
    val profileText = LocalAppText.current.profile
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = profileText.serverSettings,
                color = Color(0xFF5D6B84),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (showBackToApp) {
                    TextButton(onClick = onBackToApp) {
                        Text(profileText.backToApp)
                    }
                }
                TextButton(onClick = onToggle) {
                    Text(if (draft.expanded) profileText.collapse else profileText.expand)
                }
            }
        }
        if (draft.expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = draft.host,
                onValueChange = onHostChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(profileText.serverHostLabel) },
                placeholder = { Text(profileText.serverHostPlaceholder) }
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = draft.port,
                onValueChange = onPortChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(profileText.serverPortLabel) },
                placeholder = { Text(profileText.serverPortPlaceholder) }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = profileText.serverSummaryPrefix + draft.host.ifBlank { profileText.serverHostPlaceholder } + ":" + draft.port.ifBlank { profileText.serverPortPlaceholder },
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF7B89A1),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(12.dp))
                CampusButton(
                    text = if (saving) LocalAppText.current.common.saving else profileText.saveServerSettings,
                    onClick = onSave,
                    enabled = !saving,
                    modifier = Modifier.width(144.dp)
                )
            }
        }
    }
}

@Composable
private fun LoginModeTabs() {
    val text = LocalAppText.current.login
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF152742).copy(alpha = 0.06f), RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .background(Color.White, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text.loginTab, color = Color(0xFF173150), fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(42.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text.autoRegisterTab, color = Color(0xFF7B89A1), fontWeight = FontWeight.Bold)
        }
    }
}
