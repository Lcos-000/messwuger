package com.campusassistant.android.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.MaterialTheme
import com.campusassistant.android.ui.common.CampusSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.campusassistant.android.ui.common.CampusButton
import com.campusassistant.android.ui.common.CampusTextField
import com.campusassistant.android.ui.common.ModeDot
import com.campusassistant.android.ui.common.campusCardColorPalette
import com.campusassistant.android.ui.common.resolveCampusCardBaseColor
import com.campusassistant.android.ui.text.LocalAppText
import kotlin.math.roundToInt

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
    onPersonalizationExpandedToggle: () -> Unit,
    onSave: () -> Unit,
    onUploadRequest: (String) -> Unit
) {
    val draft = profileState.personalizationDraft
    val text = LocalAppText.current.profile
    Column(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            RowIcon(Icons.Default.Tune, Color(0xFFEFF6FF), HeroStart)
            Column(modifier = Modifier.weight(1f)) {
                Text(text.personalization, color = TextStrong, fontWeight = FontWeight.SemiBold)
                Text(text.personalizationHint, color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onPersonalizationExpandedToggle) {
                Text(if (profileState.personalizationSectionState.expanded) text.collapse else text.expand)
            }
        }

        AnimatedVisibility(
            visible = profileState.personalizationSectionState.expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                AssetChoiceRow(
                    label = text.avatar,
                    type = "avatar",
                    selected = draft.avatar,
                    options = assetOptions(
                        text.localInitial,
                        text.avatar,
                        profileState.defaultOptions?.avatars,
                        profileState.customAssets?.customAvatar
                    ),
                    onSelect = onAvatarSelect,
                    onUploadRequest = onUploadRequest
                )
                AssetChoiceRow(
                    label = text.background,
                    type = "background",
                    selected = draft.background,
                    options = assetOptions(
                        text.lightBackground,
                        text.background,
                        profileState.defaultOptions?.backgrounds,
                        profileState.customAssets?.customBackground
                    ),
                    onSelect = onBackgroundSelect,
                    onUploadRequest = onUploadRequest
                )
                AssetChoiceRow(
                    label = text.wallpaper,
                    type = "wallpaper",
                    selected = draft.wallpaper,
                    options = assetOptions(
                        text.lightWallpaper,
                        text.wallpaper,
                        profileState.defaultOptions?.wallpapers,
                        profileState.customAssets?.customWallpaper
                    ),
                    onSelect = onWallpaperSelect,
                    onUploadRequest = onUploadRequest
                )

                SliderSetting(text.cardOpacity, draft.cardOpacity, 0.2f..1f, "%.2f".format(draft.cardOpacity), onCardOpacityChange)
                CardColorSliderSetting(
                    label = text.cardBlur,
                    value = draft.cardBlur,
                    onChange = onCardBlurChange
                )
                SliderSetting(text.wallpaperMask, draft.wallpaperMask, 0f..1f, "%.2f".format(draft.wallpaperMask), onWallpaperMaskChange)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text.globalFont, color = TextMuted, style = MaterialTheme.typography.labelMedium)
                        Text(if (draft.globalFontEnabled) text.enabled else text.disabled, color = TextStrong, fontWeight = FontWeight.SemiBold)
                    }
                    Switch(checked = draft.globalFontEnabled, onCheckedChange = onGlobalFontChange)
                }

                CampusButton(
                    text = text.savePersonalization,
                    onClick = onSave,
                    enabled = !profileState.savingPersonalization,
                    loading = profileState.savingPersonalization
                )
            }
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
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (value, name) ->
                AssetTile(
                    label = name,
                    value = value,
                    selected = isSameAsset(selected, value),
                    avatarStyle = type == "avatar",
                    onClick = { onSelect(value) }
                )
            }
            AssetTile(
                label = UploadAssetLabel,
                value = "",
                selected = false,
                avatarStyle = type == "avatar",
                onClick = { onUploadRequest(type) }
            )
        }
    }
}

@Composable
private fun CardColorSliderSetting(
    label: String,
    value: Float,
    onChange: (Float) -> Unit
) {
    val text = LocalAppText.current.profile
    val palette = campusCardColorPalette()
    val selectedIndex = value.roundToInt().coerceIn(0, palette.lastIndex)
    val previewColor = resolveCampusCardBaseColor(value)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = TextMuted, style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(width = 30.dp, height = 18.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(previewColor)
                        .border(1.dp, Color(0xFFD7DFEA), RoundedCornerShape(999.dp))
                )
                Text(
                    if (selectedIndex == 0) text.cardColorWhite else "${text.cardColorPreset} $selectedIndex",
                    color = TextStrong,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        CampusSlider(
            value = value,
            onValueChange = onChange,
            valueRange = 0f..30f,
            steps = 29
        )
    }
}

@Composable
internal fun ServerSettingsSection(
    profileState: ProfileUiState,
    onServerSettingsToggle: () -> Unit,
    onServerHostChange: (String) -> Unit,
    onServerPortChange: (String) -> Unit,
    onSaveServerSettings: () -> Unit
) {
    val text = LocalAppText.current.profile
    val draft = profileState.serverSettingsDraft

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.46f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text.serverSettings, color = TextStrong, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = text.serverSummaryPrefix + draft.host.ifBlank { text.serverHostPlaceholder } + ":" + draft.port.ifBlank { text.serverPortPlaceholder },
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TextButton(onClick = onServerSettingsToggle) {
                    Text(if (draft.expanded) text.collapse else text.expand)
                }
            }

            AnimatedVisibility(
                visible = draft.expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CampusTextField(
                        value = draft.host,
                        onValueChange = onServerHostChange,
                        label = text.serverHostLabel,
                        placeholder = text.serverHostPlaceholder
                    )
                    CampusTextField(
                        value = draft.port,
                        onValueChange = onServerPortChange,
                        label = text.serverPortLabel,
                        placeholder = text.serverPortPlaceholder
                    )
                    CampusButton(
                        text = text.saveServerSettings,
                        onClick = onSaveServerSettings,
                        enabled = !profileState.savingServerConfig,
                        loading = profileState.savingServerConfig
                    )
                    Text(
                        text = text.serverSettingsHint,
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
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
        CampusSlider(
            value = value,
            onValueChange = onChange,
            valueRange = range
        )
    }
}

@Composable
private fun AssetTile(
    label: String,
    value: String,
    selected: Boolean,
    avatarStyle: Boolean,
    onClick: () -> Unit
) {
    val imageUrl = resolveAssetUrl(value)
    val shape = if (avatarStyle) CircleShape else RoundedCornerShape(10.dp)
    val previewModifier = if (avatarStyle) Modifier.size(74.dp) else Modifier.width(118.dp).height(82.dp)
    Column(
        modifier = Modifier.width(if (avatarStyle) 82.dp else 118.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Surface(
            onClick = onClick,
            modifier = previewModifier.border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) HeroStart else Color(0xFFE2E8F0),
                shape = shape
            ),
            shape = shape,
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
                            .clip(shape)
                    )
                }
                if (selected) {
                    ModeDot(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp), color = HeroStart)
                }
            }
        }
        Text(
            text = label,
            color = if (selected) HeroStart else TextMuted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

