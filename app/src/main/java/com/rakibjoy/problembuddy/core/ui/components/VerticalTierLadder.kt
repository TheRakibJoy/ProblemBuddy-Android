package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras
import com.rakibjoy.problembuddy.core.ui.theme.palette
import com.rakibjoy.problembuddy.domain.model.Tier

/**
 * Full-height tier ladder column used on Profile. Each row shows a colored
 * dot, the tier name + range, a progress bar, and a trailing indicator
 * (checkmark for past tiers, rating for the current tier, dash for future).
 */
@Composable
fun VerticalTierLadder(
    currentTier: Tier,
    currentRating: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Tier.entries.forEach { tier ->
            TierRow(
                tier = tier,
                currentTier = currentTier,
                currentRating = currentRating,
            )
            Spacer(Modifier.height(3.dp))
        }
    }
}

@Composable
private fun TierRow(
    tier: Tier,
    currentTier: Tier,
    currentRating: Int,
) {
    val extras = MaterialTheme.appExtras
    val palette = tier.palette()
    val tierColor = palette.strong
    val isCurrent = tier == currentTier
    val isPast = tier.ordinal < currentTier.ordinal
    val isFuture = tier.ordinal > currentTier.ordinal

    val rowAlpha = when {
        isCurrent -> 1f
        isPast -> 0.4f
        else -> 0.7f
    }

    val rowShape = RoundedCornerShape(8.dp)
    val bgMod = if (isCurrent) {
        Modifier
            .clip(rowShape)
            .background(extras.accentVioletDim, rowShape)
            .border(0.5.dp, MaterialTheme.colorScheme.primary, rowShape)
    } else Modifier

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(bgMod)
            .alpha(rowAlpha)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Tier dot (glow shadow for current)
        val dotMod = if (isCurrent) {
            Modifier.shadow(6.dp, CircleShape, clip = false, ambientColor = tierColor, spotColor = tierColor)
        } else Modifier
        Box(
            modifier = dotMod
                .size(8.dp)
                .clip(CircleShape)
                .background(tierColor, CircleShape),
        )
        Spacer(Modifier.width(8.dp))

        // Tier name (+ "← you" marker when current)
        val nameText = buildAnnotatedString {
            withStyle(SpanStyle(color = tierColor)) { append(tier.label) }
            if (isCurrent) {
                withStyle(SpanStyle(color = extras.accentVioletSoft, fontSize = 8.sp)) {
                    append("  ← you")
                }
            }
        }
        Text(
            text = nameText,
            fontSize = 10.sp,
            letterSpacing = 0.4.sp,
            modifier = Modifier.width(130.dp),
        )

        // Tier range
        val rangeText = if (tier == Tier.LEGENDARY) "${tier.floor}+"
        else "${tier.floor}–${tier.target - 1}"
        Text(
            text = rangeText,
            fontSize = 9.sp,
            color = extras.textTertiary,
            modifier = Modifier.width(70.dp),
        )

        // Progress bar
        val progress = when {
            isPast -> 1f
            isCurrent -> {
                val span = (tier.target - tier.floor).toFloat().coerceAtLeast(1f)
                ((currentRating - tier.floor).coerceAtLeast(0) / span).coerceIn(0f, 1f)
            }
            else -> 0f
        }
        val barTrack = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        val barFill = when {
            isCurrent -> tierColor
            isPast -> tierColor.copy(alpha = 0.27f)
            else -> Color.Transparent
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(barTrack),
        ) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(3.dp)
                        .background(barFill, RoundedCornerShape(2.dp)),
                )
            }
        }
        Spacer(Modifier.width(6.dp))

        // Trailing: ✓ | rating | —
        val trailing = when {
            isCurrent -> currentRating.toString()
            isPast -> "✓"
            isFuture -> "—"
            else -> ""
        }
        Text(
            text = trailing,
            fontSize = 9.sp,
            color = if (isFuture) extras.textTertiary else tierColor,
            modifier = Modifier.width(32.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VerticalTierLadderPreview() {
    ProblemBuddyTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            VerticalTierLadder(currentTier = Tier.LEGENDARY, currentRating = 3847)
        }
    }
}
