package com.rakibjoy.problembuddy.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Non-MD3 design tokens the redesign mockup needs. MD3's ColorScheme
 * can't cleanly express a three-tier surface hierarchy or split the
 * text ramp into primary/secondary/tertiary without abusing slots, so
 * these live alongside the scheme and are exposed via
 * `MaterialTheme.appExtras`.
 */
@Immutable
data class AppExtraColors(
    val surfaceElevated: Color,      // stat cards, problem cards in dark
    val surfaceRaised: Color,        // input fields, raised surfaces
    val borderSubtle: Color,         // default hairline (0.5dp)
    val borderStrong: Color,         // hover / raised border
    val textSecondary: Color,        // secondary label tone
    val textTertiary: Color,         // hint / disabled tone
    val accentVioletSoft: Color,     // lighter violet for active nav/text
    val accentVioletDim: Color,      // current-tier row bg on Profile
    val accentCyanSoft: Color,       // cyan accent highlight
    val deltaPositive: Color,        // green delta indicator
    val deltaNegative: Color,        // red delta indicator
)

internal val DarkExtras = AppExtraColors(
    surfaceElevated = Color(0xFF18181F),
    surfaceRaised = Color(0xFF1E1E28),
    borderSubtle = Color(0xFF2F2F3E),
    borderStrong = Color(0xFF3A3A50),
    textSecondary = Color(0xFF9090A8),
    textTertiary = Color(0xFF5A5A72),
    accentVioletSoft = Color(0xFFA78BFA),
    accentVioletDim = Color(0xFF1E1530),
    accentCyanSoft = Color(0xFF22D3EE),
    deltaPositive = Color(0xFF22C55E),
    deltaNegative = Color(0xFFEF4444),
)

// Light extras — symmetric derivations. Elevated/raised step *up* from
// the white base; border tones are soft grays; text ramp inverts the
// dark scheme; accents stay saturated enough to read on white.
internal val LightExtras = AppExtraColors(
    surfaceElevated = Color(0xFFF6F4FB),
    surfaceRaised = Color(0xFFFFFFFF),
    borderSubtle = Color(0xFFE3DFEA),
    borderStrong = Color(0xFFCAC4D0),
    textSecondary = Color(0xFF4A4758),
    textTertiary = Color(0xFF7A7789),
    accentVioletSoft = Color(0xFF7C3AED),
    accentVioletDim = Color(0xFFEDE4FF),
    accentCyanSoft = Color(0xFF0E7490),
    deltaPositive = Color(0xFF15803D),
    deltaNegative = Color(0xFFDC2626),
)

val LocalAppExtras = staticCompositionLocalOf { LightExtras }

/**
 * Access the redesign's non-MD3 tokens: `MaterialTheme.appExtras.borderSubtle`
 * etc. Uses `@ReadOnlyComposable` so it can appear in default arg positions.
 */
val MaterialTheme.appExtras: AppExtraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppExtras.current
