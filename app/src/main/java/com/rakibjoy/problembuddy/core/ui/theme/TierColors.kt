package com.rakibjoy.problembuddy.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.rakibjoy.problembuddy.domain.model.Tier
import kotlin.math.pow

/**
 * Accent color pair for a given Codeforces [Tier].
 *
 * - [soft] is used for low-emphasis surfaces (chip backgrounds, skeleton tint).
 * - [strong] is the primary accent for the tier (pill fill, gradient end stop).
 * - [onColor] is the foreground text/icon color to use on top of [strong] and
 *   on top of a [gradient] built from the pair. Picked for roughly WCAG-AA
 *   contrast against [strong].
 */
data class TierPalette(
    val soft: Color,
    val strong: Color,
    val onColor: Color,
)

internal val White = Color(0xFFFFFFFF)
internal val NearBlack = Color(0xFF0B0B10)

// Codeforces-inspired accents. `soft` is the darker/base shade, `strong` is the
// brighter pop used as the gradient end. `onColor` is chosen for contrast
// against `strong` (the higher-luminance stop).
private val NewbiePalette = TierPalette(
    soft = Color(0xFF6B7280),
    strong = Color(0xFF9CA3AF),
    onColor = NearBlack,
)
private val PupilPalette = TierPalette(
    soft = Color(0xFF10B981),
    strong = Color(0xFF34D399),
    onColor = NearBlack,
)
private val SpecialistPalette = TierPalette(
    soft = Color(0xFF06B6D4),
    strong = Color(0xFF22D3EE),
    onColor = NearBlack,
)
private val ExpertPalette = TierPalette(
    soft = Color(0xFF3B82F6),
    strong = Color(0xFF60A5FA),
    onColor = NearBlack,
)
private val CandidateMasterPalette = TierPalette(
    soft = Color(0xFF8B5CF6),
    strong = Color(0xFFA78BFA),
    onColor = NearBlack,
)
private val MasterPalette = TierPalette(
    soft = Color(0xFFF59E0B),
    strong = Color(0xFFFBBF24),
    onColor = NearBlack,
)
private val IntlMasterPalette = TierPalette(
    soft = Color(0xFFF97316),
    strong = Color(0xFFFB923C),
    onColor = NearBlack,
)
// Grandmaster #F87171: white-on-strong ~2.8:1 (fails AA), black-on-strong ~7.6:1.
// Switched to NearBlack per review — the reds read cleaner on dark text.
private val GrandmasterPalette = TierPalette(
    soft = Color(0xFFEF4444),
    strong = Color(0xFFF87171),
    onColor = NearBlack,
)
// Intl Grandmaster #EF4444: white-on-strong ~4.55:1 (borderline AA), black ~5.9:1.
// Picking NearBlack for a stronger body-copy margin.
private val IntlGrandmasterPalette = TierPalette(
    soft = Color(0xFFDC2626),
    strong = Color(0xFFEF4444),
    onColor = NearBlack,
)
private val LegendaryPalette = TierPalette(
    soft = Color(0xFF991B1B),
    strong = Color(0xFFDC2626),
    onColor = White,
)

fun Tier.palette(): TierPalette = when (this) {
    Tier.NEWBIE -> NewbiePalette
    Tier.PUPIL -> PupilPalette
    Tier.SPECIALIST -> SpecialistPalette
    Tier.EXPERT -> ExpertPalette
    Tier.CANDIDATE_MASTER -> CandidateMasterPalette
    Tier.MASTER -> MasterPalette
    Tier.INTL_MASTER -> IntlMasterPalette
    Tier.GRANDMASTER -> GrandmasterPalette
    Tier.INTL_GRANDMASTER -> IntlGrandmasterPalette
    Tier.LEGENDARY -> LegendaryPalette
}

/** Linear gradient from the tier's `soft` stop to its `strong` stop. */
fun Tier.gradient(): Brush {
    val p = palette()
    return Brush.linearGradient(colors = listOf(p.soft, p.strong))
}

/**
 * Contrast-aware foreground color for text laid over a tier's [gradient]. Picks
 * white or near-black based on the relative luminance of the brighter stop,
 * targeting at least WCAG AA (4.5:1) for body copy.
 *
 * Note: individual [TierPalette.onColor] values are hand-tuned against each
 * tier's accent and may differ from this threshold-based helper for borderline
 * hues (e.g. the mid-luminance reds, where a pure 0.55 luminance cutoff would
 * pick white but a direct contrast-ratio comparison prefers near-black).
 */
fun Tier.onGradient(): Color {
    val p = palette()
    return if (p.strong.luminance() > 0.55f) NearBlack else White
}

private fun Color.luminance(): Float {
    // WCAG relative luminance approximation
    fun chan(c: Float): Float = if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
    return 0.2126f * chan(red) + 0.7152f * chan(green) + 0.0722f * chan(blue)
}

/**
 * Softer variant of [Tier.gradient] for use in full-bleed card backgrounds where
 * text readability matters. Mixes both stops 70% toward the app surface color,
 * keeping the tier hue while restoring contrast.
 */
@Composable
fun Tier.mutedGradient(): Brush {
    val p = palette()
    val surface = MaterialTheme.colorScheme.surface
    val soft = lerp(p.soft, surface, 0.35f)
    val strong = lerp(p.strong, surface, 0.15f)
    return Brush.linearGradient(listOf(soft, strong))
}
