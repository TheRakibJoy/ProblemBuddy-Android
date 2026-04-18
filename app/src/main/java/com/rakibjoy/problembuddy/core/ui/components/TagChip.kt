package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras

/**
 * Compact rectangular tag chip (4dp radius) matching the redesign mockup.
 *
 * Variants (precedence: excluded > selected > weak > default):
 *  - Default: transparent bg + borderStrong + textSecondary
 *  - Selected: colorScheme.primary bg + white text, no border
 *  - Weak (tag the user struggles with): violet-tinted bg + border + text
 *  - Excluded: dark red bg + red border + red text with line-through
 */
@Composable
fun TagChip(
    tag: String,
    selected: Boolean = false,
    weak: Boolean = false,
    excluded: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val primary = MaterialTheme.colorScheme.primary

    data class ChipStyle(
        val bg: Color,
        val border: Color?,
        val fg: Color,
        val strike: Boolean,
    )

    val style = when {
        excluded -> ChipStyle(
            bg = Color(0xFF1A0A0A),
            border = Color(0x44EF4444),
            fg = Color(0xFFEF4444),
            strike = true,
        )
        selected -> ChipStyle(
            bg = primary,
            border = null,
            fg = Color.White,
            strike = false,
        )
        weak -> ChipStyle(
            bg = Color(0x11AA00AA),
            border = Color(0x44AA00AA),
            fg = Color(0xFFAA00AA),
            strike = false,
        )
        else -> ChipStyle(
            bg = Color.Transparent,
            border = extras.borderStrong,
            fg = extras.textSecondary,
            strike = false,
        )
    }

    val shape = RoundedCornerShape(4.dp)
    val clickableMod = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    val borderMod = if (style.border != null) {
        Modifier.border(0.5.dp, style.border, shape)
    } else Modifier

    Row(
        modifier = modifier
            .clip(shape)
            .background(style.bg, shape)
            .then(borderMod)
            .then(clickableMod)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.27.sp, // 0.03em @ 9sp
                textDecoration = if (style.strike) TextDecoration.LineThrough else null,
            ),
            color = style.fg,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TagChipsPreview() {
    ProblemBuddyTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                TagChip("dp")
                TagChip("graphs", selected = true)
                TagChip("data structures", weak = true)
                TagChip("greedy", excluded = true)
            }
        }
    }
}
