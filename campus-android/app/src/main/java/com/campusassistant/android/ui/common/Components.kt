package com.campusassistant.android.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.campusassistant.android.ui.theme.CampusCardShape

val CampusPagePadding = 16.dp
val CampusButtonHeight = 48.dp
val CampusCardPadding = 14.dp
val CampusMutedText = Color(0xFF64748B)
val CampusSuccessText = Color(0xFF0F766E)

@Immutable
data class CampusCardStyle(
    val opacity: Float = 1f,
    val blur: Float = 14f
)

val LocalCampusCardStyle = compositionLocalOf { CampusCardStyle() }

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
fun campusGlassStrength(): Float = (LocalCampusCardStyle.current.blur / 30f).coerceIn(0f, 1f)

@Composable
fun campusGlassContainerColor(base: Color = MaterialTheme.colorScheme.surface): Color {
    val style = LocalCampusCardStyle.current
    return base.copy(alpha = style.opacity.coerceIn(0.2f, 1f))
}

@Composable
fun campusGlassBorder(): BorderStroke {
    val strength = campusGlassStrength()
    return BorderStroke((0.8f + strength * 0.7f).dp, Color.White.copy(alpha = 0.42f + strength * 0.22f))
}

@Composable
fun campusGlassElevation(base: Dp = 1.dp): Dp {
    val strength = campusGlassStrength()
    return base + (strength * 7).dp
}

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
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = CampusMutedText,
                    style = MaterialTheme.typography.bodyMedium
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
        shape = CampusCardShape
    ) {
        Text(text = text)
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
        shape = CampusCardShape
    ) {
        Text(text = text)
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
        visualTransformation = visualTransformation
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
        elevation = CardDefaults.cardElevation(defaultElevation = campusGlassElevation())
    ) {
        Column(modifier = Modifier.padding(CampusCardPadding), content = content)
    }
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
    Text(
        text = message,
        modifier = modifier.fillMaxWidth(),
        color = if (isError) MaterialTheme.colorScheme.error else CampusSuccessText,
        style = MaterialTheme.typography.bodySmall
    )
}
