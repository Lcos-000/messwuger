package com.campusassistant.android.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusassistant.android.ui.theme.CampusBlue
import com.campusassistant.android.ui.theme.CampusCardShape
import kotlin.math.roundToInt

val CampusPagePadding = 18.dp
val CampusButtonHeight = 56.dp
val CampusCardPadding = 16.dp
val CampusMutedText = Color(0xFF5F6F85)
val CampusSuccessText = Color(0xFF047857)

val CampusModeDotActive = Color(0xFF4F86F7)

@Composable
fun ModeDot(
    modifier: Modifier = Modifier,
    color: Color = CampusModeDotActive
) {
    Box(modifier = modifier.size(8.dp).background(color, CircleShape))
}

private val CampusCardPalette = listOf(
    Color(0xFFFFFFFF),
    Color(0xFFF7F7F8),
    Color(0xFFF5F7FA),
    Color(0xFFF4F4F5),
    Color(0xFFF6F1EE),
    Color(0xFFF7F3E8),
    Color(0xFFF7EEDF),
    Color(0xFFF4E7D8),
    Color(0xFFF7F0F5),
    Color(0xFFF3E8F3),
    Color(0xFFEFE5F6),
    Color(0xFFE7E8F7),
    Color(0xFFE6ECFA),
    Color(0xFFE9F1FB),
    Color(0xFFE5F3F7),
    Color(0xFFE6F5F2),
    Color(0xFFEAF6EC),
    Color(0xFFF0F6E7),
    Color(0xFFF5F4E7),
    Color(0xFFFAF3E7),
    Color(0xFFF8EEE8),
    Color(0xFFF7E9EA),
    Color(0xFFF4E6EA),
    Color(0xFFEDE8F2),
    Color(0xFFE8EDF5),
    Color(0xFFE8F1F2),
    Color(0xFFEAF3EF),
    Color(0xFFF1F4EE),
    Color(0xFFF5F2ED),
    Color(0xFFF3EFEA),
    Color(0xFFECECEC)
)

@Immutable
data class CampusCardStyle(
    val opacity: Float = 1f,
    val blur: Float = 0f
)

val LocalCampusCardStyle = compositionLocalOf { CampusCardStyle() }

fun campusCardColorPalette(): List<Color> = CampusCardPalette

fun resolveCampusCardBaseColor(index: Float): Color {
    val safeIndex = index.roundToInt().coerceIn(0, CampusCardPalette.lastIndex)
    return CampusCardPalette[safeIndex]
}

@Composable
fun CampusCardStyleProvider(
    opacity: Float,
    blur: Float,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCampusCardStyle provides CampusCardStyle(
            opacity = opacity.coerceIn(0.2f, 1f),
            blur = blur.coerceIn(0f, 30f)
        ),
        content = content
    )
}

@Composable
fun campusGlassContainerColor(base: Color = resolveCampusCardBaseColor(LocalCampusCardStyle.current.blur)): Color {
    val style = LocalCampusCardStyle.current
    return base.copy(alpha = style.opacity.coerceIn(0.34f, 1f))
}

@Composable
fun campusGlassBorder(): BorderStroke = BorderStroke(1.dp, Color.White.copy(alpha = 0.62f))

@Composable
fun campusGlassElevation(base: Dp = 0.dp): Dp = base

@Composable
fun CampusPage(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(CampusPagePadding),
        content = content
    )
}

@Composable
fun CampusPageHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = CampusMutedText,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }
        action?.invoke()
    }
}

@Composable
fun CampusButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(CampusButtonHeight),
        enabled = enabled && !loading,
        shape = CampusCardShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
fun CampusOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(CampusButtonHeight),
        enabled = enabled,
        shape = CampusCardShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.82f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            maxLines = 2
        )
    }
}

@Composable
fun CampusTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    placeholder: String? = null
) {
    val labelContent = label.takeIf { it.isNotBlank() }?.let { text -> @Composable { Text(text) } }
    val placeholderContent = placeholder?.takeIf { it.isNotBlank() }?.let { text -> @Composable { Text(text) } }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = labelContent,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        shape = CampusCardShape,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        placeholder = placeholderContent,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.78f),
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f)
        )
    )
}

@Composable
fun CampusCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CampusCardShape,
        colors = CardDefaults.cardColors(containerColor = campusGlassContainerColor()),
        border = campusGlassBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = campusGlassElevation(0.dp))
    ) {
        Column(modifier = Modifier.padding(CampusCardPadding), content = content)
    }
}

@Composable
fun CampusSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        CampusSectionTitle(title = title)
        content()
    }
}

@Composable
fun CampusSectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        modifier = modifier,
        color = Color(0xFF64748B),
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.labelMedium
    )
}

@Composable
fun CampusLoadingState(
    text: String = "加载中",
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            Text(text = text, color = CampusMutedText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun CampusEmptyState(
    text: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.74f), CampusCardShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.62f), CampusCardShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = CampusMutedText,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(text = text, color = CampusMutedText, style = MaterialTheme.typography.bodyLarge)
            if (actionText != null && onAction != null) {
                CampusOutlinedButton(text = actionText, onClick = onAction, modifier = Modifier.fillMaxWidth(0.56f))
            }
        }
    }
}

@Composable
fun CampusMessage(
    message: String?,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val visible = !message.isNullOrBlank()
    val background = if (isError) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
    } else {
        CampusSuccessText.copy(alpha = 0.08f)
    }
    val foreground = if (isError) MaterialTheme.colorScheme.error else CampusSuccessText
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Text(
            text = message.orEmpty(),
            modifier = modifier
                .fillMaxWidth()
                .background(background, CampusCardShape)
                .border(1.dp, foreground.copy(alpha = 0.12f), CampusCardShape)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            color = foreground,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun CampusStatusMessages(
    statusMessage: String?,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 10.dp
) {
    if (statusMessage.isNullOrBlank() && errorMessage.isNullOrBlank()) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        if (!statusMessage.isNullOrBlank()) {
            CampusMessage(message = statusMessage, isError = false)
        }
        if (!errorMessage.isNullOrBlank()) {
            CampusMessage(message = errorMessage, isError = true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val thumbScale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "thumbScale"
    )

    val sliderColors = SliderDefaults.colors(
        thumbColor = CampusBlue,
        activeTrackColor = CampusBlue,
        inactiveTrackColor = CampusBlue.copy(alpha = 0.14f),
        disabledThumbColor = CampusBlue.copy(alpha = 0.4f),
        disabledActiveTrackColor = CampusBlue.copy(alpha = 0.4f),
        disabledInactiveTrackColor = CampusBlue.copy(alpha = 0.08f)
    )

    val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        enabled = enabled,
        modifier = modifier,
        interactionSource = interactionSource,
        colors = sliderColors,
        thumb = {
            Box(
                modifier = Modifier
                    .size(20.dp * thumbScale)
                    .shadow(
                        elevation = if (isPressed) 8.dp else 4.dp,
                        shape = CircleShape
                    )
                    .background(CampusBlue, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )
        },
        track = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(CampusBlue.copy(alpha = 0.14f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(CampusBlue)
                )
            }
        }
    )
}

@Composable
fun Modifier.verticalFadeEdges(
    scrollState: ScrollState,
    fadeHeight: Dp = 24.dp,
    edgeColor: Color = MaterialTheme.colorScheme.background
): Modifier {
    val fadePx = with(LocalDensity.current) { fadeHeight.toPx() }
    val topAlpha by remember(fadePx) {
        derivedStateOf {
            (scrollState.value / fadePx).coerceIn(0f, 1f)
        }
    }
    val bottomAlpha by remember(fadePx) {
        derivedStateOf {
            ((scrollState.maxValue - scrollState.value) / fadePx).coerceIn(0f, 1f)
        }
    }

    return this
        .drawWithContent {
            drawContent()

            if (topAlpha > 0f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            edgeColor.copy(alpha = topAlpha),
                            edgeColor.copy(alpha = 0f)
                        ),
                        startY = 0f,
                        endY = fadePx
                    )
                )
            }

            if (bottomAlpha > 0f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            edgeColor.copy(alpha = 0f),
                            edgeColor.copy(alpha = bottomAlpha)
                        ),
                        startY = size.height - fadePx,
                        endY = size.height
                    )
                )
            }
        }
}
