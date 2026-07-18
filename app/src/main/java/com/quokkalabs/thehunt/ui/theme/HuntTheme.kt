package com.quokkalabs.thehunt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.quokkalabs.thehunt.R

// Palette
val Ink = Color(0xFF0B0B0D)
val InkRaised = Color(0xFF131118)
val Bone = Color(0xFFE8E4DA)
val Violet = Color(0xFF7C4DFF)
val VioletDeep = Color(0xFF3A2470)
val Mist = Color(0xFF4A4458) // thin grey-purple borders

// Bundled fonts — no network, ever.
val Gothic = FontFamily(
    Font(R.font.cinzel_decorative_regular, FontWeight.Normal),
    Font(R.font.cinzel_decorative_bold, FontWeight.Bold),
)

val Typewriter = FontFamily(
    Font(R.font.special_elite, FontWeight.Normal),
)

private val HuntColorScheme = darkColorScheme(
    primary = Violet,
    onPrimary = Bone,
    secondary = Mist,
    onSecondary = Bone,
    background = Ink,
    onBackground = Bone,
    surface = InkRaised,
    onSurface = Bone,
    outline = Mist,
)

private val HuntTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Gothic,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        letterSpacing = 4.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Gothic,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        letterSpacing = 2.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Gothic,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        letterSpacing = 2.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Typewriter,
        fontSize = 17.sp,
        lineHeight = 28.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Typewriter,
        fontSize = 15.sp,
        lineHeight = 24.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Typewriter,
        fontSize = 16.sp,
        letterSpacing = 3.sp,
    ),
)

@Composable
fun HuntTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HuntColorScheme,
        typography = HuntTypography,
        content = content,
    )
}
