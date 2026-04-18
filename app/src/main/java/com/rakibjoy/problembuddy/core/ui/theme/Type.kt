package com.rakibjoy.problembuddy.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Typography strategy
// -------------------
// The redesign calls for JetBrains Mono across the app. Rather than pull in
// a Google Fonts dependency (which needs network at first paint), we use
// the platform monospace family which on Android resolves to Droid Sans
// Mono / Roboto Mono — visually close enough for the redesign's intent.
// The scale is deliberately tight: the mockup leans on compact sizes
// (9–18sp) and aggressive negative tracking on display/headline.
//
// Note on letter-spacing: Compose's `letterSpacing` is sp-based. Values
// below correspond roughly to the CSS em-based tracking in the mockup:
//   -0.06em at 26sp ≈ -1.56sp, -0.04em at 18sp ≈ -0.72sp, etc.
private val MonoFamily: FontFamily = FontFamily.Monospace

val AppTypography: Typography = Typography(
    // ---- Display: big numeric/header moments. Tight negative tracking. ----
    displayLarge = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-2.0).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-1.8).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        letterSpacing = (-1.56).sp, // -0.06em @ 26sp
    ),

    // ---- Headline: section headers ----------------------------------------
    headlineLarge = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.88).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.8).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.72).sp, // -0.04em @ 18sp
    ),

    // ---- Title: card titles, list row heads -------------------------------
    titleLarge = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.32).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.24.sp, // 0.02em @ 12sp
    ),

    // ---- Body: reading text. lineHeight ~1.5 per mockup -------------------
    bodyLarge = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp, // ≈ 1.5
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),

    // ---- Label: buttons, chips, uppercase captions ------------------------
    labelLarge = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.24.sp, // 0.02em @ 12sp
    ),
    labelMedium = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.6.sp, // 0.06em @ 10sp (uppercase-friendly)
    ),
    labelSmall = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.72.sp, // 0.08em @ 9sp
    ),
)

// Back-compat alias: legacy call sites referenced `Typography` directly.
val Typography: Typography = AppTypography
