package com.rakibjoy.problembuddy.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Typography fallback strategy
// ----------------------------
// The design calls for Space Grotesk (display) + Inter (body) via the
// Compose Google Fonts runtime provider. That provider requires network at
// first paint, and this session runs without internet access. Rather than
// ship a theme whose first render stalls or flashes, we fall back to
// carefully tuned system FontFamilies (SansSerif body, Default display)
// with deliberate weight + letterSpacing choices so the app still feels
// distinctive. Swap these two families to the Google Fonts variants later
// and the rest of the type scale stays valid.
private val DisplayFamily: FontFamily = FontFamily.Default
private val BodyFamily: FontFamily = FontFamily.SansSerif

val AppTypography: Typography = Typography(
    // ---- Display: big, tight, confident. Negative tracking + bold weight --
    displayLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.25).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),

    // ---- Headline: section headers ----------------------------------------
    headlineLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),

    // ---- Title: card titles, dialog titles --------------------------------
    titleLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // ---- Body: reading text -----------------------------------------------
    bodyLarge = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),

    // ---- Label: buttons, chips, captions ----------------------------------
    labelLarge = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

// Back-compat: the original file exposed a top-level `Typography` val. Keep an
// alias so any unknown call sites still resolve.
val Typography: Typography = AppTypography
