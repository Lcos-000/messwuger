package com.campusassistant.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.campusassistant.android.ui.profile.ProfileUiState
import com.campusassistant.android.ui.profile.resolveAssetUrl

@Composable
fun GlobalWallpaperLayer(profileState: ProfileUiState) {
    val draft = profileState.personalizationDraft
    val wallpaperUrl = resolveAssetUrl(draft.wallpaper)
    val mask = draft.wallpaperMask.coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF7F9FC),
                        Color(0xFFE9EEF6),
                        Color(0xFFF4F7FB)
                    )
                )
            )
    )

    if (wallpaperUrl != null) {
        AsyncImage(
            model = wallpaperUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 1f - mask
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFF7F9FC).copy(alpha = mask * 0.94f),
                            Color(0xFFE9EEF6).copy(alpha = mask * 0.46f),
                            Color(0xFFF4F7FB).copy(alpha = mask * 0.78f)
                        )
                    )
                )
        )
    }
}
