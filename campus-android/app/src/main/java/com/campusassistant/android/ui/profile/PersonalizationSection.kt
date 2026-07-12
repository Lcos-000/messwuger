package com.campusassistant.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
internal fun PersonalizationSection(
    profileState: ProfileUiState,
    onAvatarSelect: (String) -> Unit,
    onBackgroundSelect: (String) -> Unit,
    onWallpaperSelect: (String) -> Unit,
    onCardOpacityChange: (Float) -> Unit,
    onCardBlurChange: (Float) -> Unit,
    onWallpaperMaskChange: (Float) -> Unit,
    onGlobalFontChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onUploadRequest: (String) -> Unit
) {
    val draft = profileState.personalizationDraft
    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            RowIcon(Icons.Default.Tune, Color(0xFFEFF6FF), HeroStart)
            Column(modifier = Modifier.weight(1f)) {
                Text("个性化设置", color = TextStrong, fontWeight = FontWeight.SemiBold)
                Text("点击自定义资源或上传卡片可选择相册图片；裁剪下一轮接入", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
        }

        AssetChoiceRow("头像", "avatar", draft.avatar, assetOptions("本地首字", profileState.defaultOptions?.avatars, profileState.customAssets?.customAvatar), onAvatarSelect, onUploadRequest)
        AssetChoiceRow("顶部背景", "background", draft.background, assetOptions("浅色背景", profileState.defaultOptions?.backgrounds, profileState.customAssets?.customBackground), onBackgroundSelect, onUploadRequest)
        AssetChoiceRow("墙纸", "wallpaper", draft.wallpaper, assetOptions("浅灰墙纸", profileState.defaultOptions?.wallpapers, profileState.customAssets?.customWallpaper), onWallpaperSelect, onUploadRequest)

        SliderSetting("资料卡不透明度", draft.cardOpacity, 0.2f..1f, "%.2f".format(draft.cardOpacity), onCardOpacityChange)
        SliderSetting("资料卡模糊度", draft.cardBlur, 0f..30f, "%.0f".format(draft.cardBlur), onCardBlurChange)
        SliderSetting("墙纸蒙版强度", draft.wallpaperMask, 0f..1f, "%.2f".format(draft.wallpaperMask), onWallpaperMaskChange)

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("全局字体", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                Text(if (draft.globalFontEnabled) "已开启" else "已关闭", color = TextStrong, fontWeight = FontWeight.SemiBold)
            }
            Switch(checked = draft.globalFontEnabled, onCheckedChange = onGlobalFontChange)
        }

        Button(onClick = onSave, modifier = Modifier.fillMaxWidth(), enabled = !profileState.savingPersonalization) {
            Text(if (profileState.savingPersonalization) "保存中" else "保存个性化配置")
        }
    }
}

@Composable
private fun AssetChoiceRow(
    label: String,
    type: String,
    selected: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    onUploadRequest: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = TextMuted, style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, name) ->
                AssetTile(
                    label = name,
                    value = value,
                    selected = isSameAsset(selected, value),
                    onClick = {
                        if (name == CustomAssetLabel) onUploadRequest(type) else onSelect(value)
                    }
                )
            }
            AssetTile(
                label = UploadAssetLabel,
                value = "",
                selected = false,
                onClick = { onUploadRequest(type) }
            )
        }
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueText: String,
    onChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelMedium)
            Text(valueText, color = TextStrong, fontWeight = FontWeight.SemiBold)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun AssetTile(
    label: String,
    value: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val imageUrl = resolveAssetUrl(value)
    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(118.dp)
            .height(82.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) HeroStart else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.18f)))
            } else if (label == UploadAssetLabel) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = HeroStart, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))))
                )
            }
            Text(
                text = label,
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                color = if (imageUrl != null) Color.White else TextStrong,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (selected) {
                ModeDot(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp))
            }
        }
    }
}

@Composable
private fun ModeDot(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(8.dp).background(HeroStart, CircleShape))
}
