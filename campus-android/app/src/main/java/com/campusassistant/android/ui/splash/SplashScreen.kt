package com.campusassistant.android.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campusassistant.android.R
import com.campusassistant.android.ui.text.LocalAppText

@Composable
fun SplashScreen() {
    val text = LocalAppText.current.splash
    val scale = remember { Animatable(0.88f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(durationMillis = 220, easing = FastOutSlowInEasing))
        scale.animateTo(1.08f, tween(durationMillis = 260, easing = FastOutSlowInEasing))
        scale.animateTo(0.97f, tween(durationMillis = 130, easing = FastOutSlowInEasing))
        scale.animateTo(1f, tween(durationMillis = 170, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFBF5E4),
                        Color(0xFFFCF8EC),
                        Color(0xFFFAF1D8)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo_splash),
                contentDescription = text.logoDescription,
                modifier = Modifier
                    .size(196.dp)
                    .scale(scale.value)
                    .alpha(alpha.value),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = text.appName,
                modifier = Modifier.alpha(alpha.value),
                color = Color(0xFF8B6516),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
