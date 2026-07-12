package com.campusassistant.android.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.campusassistant.android.data.model.UserPersonal
import com.campusassistant.android.ui.common.campusGlassBorder
import com.campusassistant.android.ui.common.campusGlassContainerColor
import com.campusassistant.android.ui.common.campusGlassElevation
import okhttp3.MultipartBody

@Composable
fun ProfileScreen(
    tokenExists: Boolean,
    profileState: ProfileUiState,
    onRefresh: () -> Unit,
    onAutoPunchChange: (Boolean) -> Unit,
    onAvatarSelect: (String) -> Unit,
    onBackgroundSelect: (String) -> Unit,
    onWallpaperSelect: (String) -> Unit,
    onCardOpacityChange: (Float) -> Unit,
    onCardBlurChange: (Float) -> Unit,
    onWallpaperMaskChange: (Float) -> Unit,
    onGlobalFontChange: (Boolean) -> Unit,
    onSavePersonalization: () -> Unit,
    onAssetUpload: (String, MultipartBody.Part) -> Unit,
    onLogout: () -> Unit
) {
    val draft = profileState.personalizationDraft
    val context = LocalContext.current
    var pendingUploadType by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val type = pendingUploadType
        pendingUploadType = null
        if (uri != null && type != null) {
            createImagePart(context, uri)?.let { part -> onAssetUpload(type, part) }
        }
    }
    val onUploadRequest: (String) -> Unit = { type ->
        pendingUploadType = type
        imagePicker.launch("image/*")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ProfileHero(
                personal = profileState.personal,
                draft = draft,
                loading = profileState.loading,
                onRefresh = onRefresh
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-42).dp)
                    .padding(horizontal = 18.dp)
            ) {
                ProfileContentPanel(
                    tokenExists = tokenExists,
                    profileState = profileState,
                    onAutoPunchChange = onAutoPunchChange,
                    onAvatarSelect = onAvatarSelect,
                    onBackgroundSelect = onBackgroundSelect,
                    onWallpaperSelect = onWallpaperSelect,
                    onCardOpacityChange = onCardOpacityChange,
                    onCardBlurChange = onCardBlurChange,
                    onWallpaperMaskChange = onWallpaperMaskChange,
                    onGlobalFontChange = onGlobalFontChange,
                    onSavePersonalization = onSavePersonalization,
                    onUploadRequest = onUploadRequest,
                    onLogout = onLogout
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileHero(
    personal: UserPersonal?,
    draft: PersonalizationDraft,
    loading: Boolean,
    onRefresh: () -> Unit
) {
    val name = personal?.name?.takeIf { it.isNotBlank() } ?: "未同步用户"
    val studentId = personal?.studentId?.takeIf { it.isNotBlank() } ?: "学号未知"
    val avatarText = name.firstOrNull()?.toString() ?: "?"
    val backgroundUrl = resolveAssetUrl(draft.background)
    val avatarUrl = resolveAssetUrl(draft.avatar)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(286.dp)
            .padding(start = 10.dp, end = 10.dp, top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(248.dp)
                .clip(RoundedCornerShape(bottomStart = 38.dp, bottomEnd = 38.dp))
                .background(Color.White.copy(alpha = 0.72f))
                .align(Alignment.TopCenter)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(236.dp)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
                .background(Brush.linearGradient(listOf(HeroStart, HeroEnd)))
                .align(Alignment.TopCenter)
        ) {
            if (backgroundUrl != null) {
                AsyncImage(
                    model = backgroundUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.18f)))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(HeroStart, HeroEnd)))
                )
            }

            OutlinedButton(
                onClick = onRefresh,
                enabled = !loading,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 18.dp)
            ) {
                Text(if (loading) "刷新中" else "刷新")
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarView(text = avatarText, url = avatarUrl)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = studentId,
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun AvatarView(text: String, url: String?) {
    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.28f))
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        if (url == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.94f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = text, color = HeroStart, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            }
        } else {
            AsyncImage(
                model = url,
                contentDescription = "头像",
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun ProfileContentPanel(
    tokenExists: Boolean,
    profileState: ProfileUiState,
    onAutoPunchChange: (Boolean) -> Unit,
    onAvatarSelect: (String) -> Unit,
    onBackgroundSelect: (String) -> Unit,
    onWallpaperSelect: (String) -> Unit,
    onCardOpacityChange: (Float) -> Unit,
    onCardBlurChange: (Float) -> Unit,
    onWallpaperMaskChange: (Float) -> Unit,
    onGlobalFontChange: (Boolean) -> Unit,
    onSavePersonalization: () -> Unit,
    onUploadRequest: (String) -> Unit,
    onLogout: () -> Unit
) {
    val cardShape = RoundedCornerShape(18.dp)
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            color = campusGlassContainerColor(CardBackground),
            border = campusGlassBorder(),
            tonalElevation = campusGlassElevation(0.dp),
            shadowElevation = campusGlassElevation(8.dp)
        ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            InfoRows(personal = profileState.personal, tokenExists = tokenExists)
            SectionDivider()
            StatusRows(
                status = profileState.status,
                loading = profileState.loading,
                updatingAutoPunch = profileState.updatingAutoPunch,
                autoPunchEnabled = profileState.autoPunchEnabled,
                onAutoPunchChange = onAutoPunchChange
            )
            MessageRows(profileState = profileState)
            SectionDivider()
            PersonalizationSection(
                profileState = profileState,
                onAvatarSelect = onAvatarSelect,
                onBackgroundSelect = onBackgroundSelect,
                onWallpaperSelect = onWallpaperSelect,
                onCardOpacityChange = onCardOpacityChange,
                onCardBlurChange = onCardBlurChange,
                onWallpaperMaskChange = onWallpaperMaskChange,
                onGlobalFontChange = onGlobalFontChange,
                onSave = onSavePersonalization,
                onUploadRequest = onUploadRequest
            )
            SectionDivider()
            ActionRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                iconBackground = Color(0xFFFFE4E6),
                iconColor = Color(0xFFE5484D),
                label = "退出登录",
                value = null,
                onClick = onLogout
            )
        }
    }
    }
}
