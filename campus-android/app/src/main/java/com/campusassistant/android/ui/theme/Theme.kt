package com.campusassistant.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

private val CampusBlue = Color(0xFF2F6FED)
private val CampusGreen = Color(0xFF18A058)
private val CampusBackground = Color(0xFFF7F9FC)
private val CampusSurface = Color(0xFFFFFFFF)
private val CampusText = Color(0xFF182033)

private val LightColors: ColorScheme = lightColorScheme(
    primary = CampusBlue,
    secondary = CampusGreen,
    background = CampusBackground,
    surface = CampusSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = CampusText,
    onSurface = CampusText
)

val CampusCardShape = RoundedCornerShape(8.dp)

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
    val family = if (globalFontEnabled) FontFamily.SansSerif else FontFamily.Default
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
