package com.campusassistant.android.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusassistant.android.ui.theme.CampusCardShape
import kotlin.math.roundToInt

val CampusPagePadding = 18.dp
val CampusButtonHeight = 56.dp
val CampusCardPadding = 16.dp
val CampusMutedText = Color(0xFF5F6F85)
val CampusSuccessText = Color(0xFF047857)

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
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(CampusButtonHeight),
        enabled = enabled,
        shape = CampusCardShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
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
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        shape = CampusCardShape,
        visualTransformation = visualTransformation,
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
                Text("-", color = CampusMutedText, style = MaterialTheme.typography.titleMedium)
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
    if (message.isNullOrBlank()) return
    val background = if (isError) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
    } else {
        CampusSuccessText.copy(alpha = 0.08f)
    }
    val foreground = if (isError) MaterialTheme.colorScheme.error else CampusSuccessText
    Text(
        text = message,
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
