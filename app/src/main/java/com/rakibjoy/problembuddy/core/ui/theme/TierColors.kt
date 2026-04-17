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
 * Values track the canonical Codeforces rank colors as closely as possible —
 * `strong` is the exact hex Codeforces uses on the site; `soft` is a darker
 * sibling used for low-emphasis surfaces (chip backgrounds, skeletons, gradient
 * starting stops). Tiers that Codeforces renders in the same color (Master /
 * International Master; Grandmaster / International Grandmaster) intentionally
 * share their palette here too — our UI distinguishes them through position
 * and label, not hue.
 *
 * Codeforces reference:
 *   https://codeforces.com/blog/entry/20638
 */
data class TierPalette(
    val soft: Color,
    val strong: Color,
    val onColor: Color,
)

internal val White = Color(0xFFFFFFFF)
internal val NearBlack = Color(0xFF0B0B10)

// Codeforces rank colors. `strong` matches the site; `soft` is a pragmatic
// darker shade for gradient stops & backgrounds. `onColor` is picked for WCAG-AA
// body-copy contrast on `strong`.
private val NewbiePalette = TierPalette(
    soft = Color(0xFF696969),
    strong = Color(0xFF808080), // CF gray
    onColor = NearBlack,
)
private val PupilPalette = TierPalette(
    soft = Color(0xFF006400),
    strong = Color(0xFF008000), // CF green
    onColor = White,
)
private val SpecialistPalette = TierPalette(
    soft = Color(0xFF007F79),
    strong = Color(0xFF03A89E), // CF cyan-teal
    onColor = White,
)
private val ExpertPalette = TierPalette(
    soft = Color(0xFF0000CC),
    strong = Color(0xFF0000FF), // CF blue
    onColor = White,
)
private val CandidateMasterPalette = TierPalette(
    soft = Color(0xFF800080),
    strong = Color(0xFFAA00AA), // CF magenta/purple
    onColor = White,
)
// Master and International Master share the same orange on Codeforces.
private val MasterOrange = TierPalette(
    soft = Color(0xFFE07A00),
    strong = Color(0xFFFF8C00), // CF orange
    onColor = NearBlack,
)
// Grandmaster and International Grandmaster share the same red on Codeforces.
private val GrandmasterRed = TierPalette(
    soft = Color(0xFFCC0000),
    strong = Color(0xFFFF0000), // CF red
    onColor = NearBlack, // black-on-red ~5.2:1; white is ~4:1, borderline AA
)
// Legendary Grandmaster also uses CF red, but Codeforces renders the handle's
// first character in black — captured elsewhere via the `HandleText` component.
// The palette here keeps a deeper soft stop so the Legendary tier still reads
// subtly differently from the IGM pill in the ladder.
private val LegendaryPalette = TierPalette(
    soft = Color(0xFF990000),
    strong = Color(0xFFFF0000),
    onColor = White,
)

fun Tier.palette(): TierPalette = when (this) {
    Tier.NEWBIE -> NewbiePalette
    Tier.PUPIL -> PupilPalette
    Tier.SPECIALIST -> SpecialistPalette
    Tier.EXPERT -> ExpertPalette
    Tier.CANDIDATE_MASTER -> CandidateMasterPalette
    Tier.MASTER -> MasterOrange
    Tier.INTL_MASTER -> MasterOrange
    Tier.GRANDMASTER -> GrandmasterRed
    Tier.INTL_GRANDMASTER -> GrandmasterRed
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
 * hues (e.g. mid-luminance reds, where a pure 0.55 luminance cutoff picks white
 * but a direct contrast-ratio comparison prefers near-black).
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
 * text readability matters. Mixes both stops toward the app surface color,
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
