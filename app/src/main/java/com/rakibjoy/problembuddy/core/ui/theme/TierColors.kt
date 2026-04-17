package com.rakibjoy.problembuddy.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.rakibjoy.problembuddy.domain.model.Tier

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

private val White = Color(0xFFFFFFFF)
private val NearBlack = Color(0xFF0B0B10)

// Codeforces-inspired accents. `soft` is the darker/base shade, `strong` is the
// brighter pop used as the gradient end. `onColor` is chosen for contrast
// against `strong` (the higher-luminance stop).
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
private val GrandmasterPalette = TierPalette(
    soft = Color(0xFFEF4444),
    strong = Color(0xFFF87171),
    onColor = White,
)
private val IntlGrandmasterPalette = TierPalette(
    soft = Color(0xFFDC2626),
    strong = Color(0xFFEF4444),
    onColor = White,
)
private val LegendaryPalette = TierPalette(
    soft = Color(0xFF991B1B),
    strong = Color(0xFFDC2626),
    onColor = White,
)

fun Tier.palette(): TierPalette = when (this) {
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
