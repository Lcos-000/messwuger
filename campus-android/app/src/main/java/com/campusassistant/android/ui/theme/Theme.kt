package com.campusassistant.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.campusassistant.android.R
import androidx.compose.ui.unit.dp

private val CampusNavy = Color(0xFF18365E)
private val CampusBlue = Color(0xFF2F6FED)
private val CampusTeal = Color(0xFF0F766E)
private val CampusBackground = Color(0xFFF3F6FA)
private val CampusSurface = Color(0xFFFBFCFE)
private val CampusText = Color(0xFF172033)
private val CampusOutline = Color(0xFFD9E2EE)
private val CampusError = Color(0xFFB42318)

private val SourceHanSerifSubset = FontFamily(Font(R.font.source_han_serif_cn_subset))

private val LightColors: ColorScheme = lightColorScheme(
    primary = CampusNavy,
    secondary = CampusBlue,
    tertiary = CampusTeal,
    background = CampusBackground,
    surface = CampusSurface,
    surfaceVariant = Color(0xFFE8EEF6),
    outline = CampusOutline,
    error = CampusError,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = CampusText,
    onSurface = CampusText,
    onSurfaceVariant = Color(0xFF526176)
)

val CampusCardShape = RoundedCornerShape(14.dp)

@Composable
fun CampusAssistantTheme(
    globalFontEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = campusTypography(globalFontEnabled),
        content = content
    )
}

private fun campusTypography(globalFontEnabled: Boolean): Typography {
    val base = Typography()
    val family = if (globalFontEnabled) SourceHanSerifSubset else FontFamily.Default
    fun TextStyle.withCampusFont(): TextStyle = copy(fontFamily = family)
    return Typography(
        displayLarge = base.displayLarge.withCampusFont(),
        displayMedium = base.displayMedium.withCampusFont(),
        displaySmall = base.displaySmall.withCampusFont(),
        headlineLarge = base.headlineLarge.withCampusFont(),
        headlineMedium = base.headlineMedium.withCampusFont(),
        headlineSmall = base.headlineSmall.withCampusFont(),
        titleLarge = base.titleLarge.withCampusFont(),
        titleMedium = base.titleMedium.withCampusFont(),
        titleSmall = base.titleSmall.withCampusFont(),
        bodyLarge = base.bodyLarge.withCampusFont(),
        bodyMedium = base.bodyMedium.withCampusFont(),
        bodySmall = base.bodySmall.withCampusFont(),
        labelLarge = base.labelLarge.withCampusFont(),
        labelMedium = base.labelMedium.withCampusFont(),
        labelSmall = base.labelSmall.withCampusFont()
    )
}
