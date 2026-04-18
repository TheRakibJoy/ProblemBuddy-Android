package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.AppShapes
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras

enum class UpsolveBadge { Unattempted, Penalty, Solved }

/**
 * Compact horizontal card for an item in the upsolve queue. Title + meta on
 * the left; small colored status badge on the right.
 */
@Composable
fun UpsolveCard(
    name: String,
    meta: String,
    badge: UpsolveBadge,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val shape = AppShapes.small

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.surfaceElevated, shape)
            .border(0.5.dp, extras.borderSubtle, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = meta,
                style = MaterialTheme.typography.labelSmall,
                color = extras.textTertiary,
            )
        }
        Badge(badge)
    }
}

@Composable
private fun Badge(badge: UpsolveBadge) {
    data class BadgeStyle(val bg: Color, val border: Color, val fg: Color, val label: String)

    val style = when (badge) {
        UpsolveBadge.Unattempted -> BadgeStyle(
            bg = Color(0xFF1A1020),
            border = Color(0x44D946EF),
            fg = Color(0xFFD946EF),
            label = "unattempted",
        )
        UpsolveBadge.Penalty -> BadgeStyle(
            bg = Color(0xFF1F0A0A),
            border = Color(0x44EF4444),
            fg = Color(0xFFEF4444),
            label = "penalty",
        )
        UpsolveBadge.Solved -> BadgeStyle(
            bg = Color(0xFF0A1F10),
            border = Color(0x4422C55E),
            fg = Color(0xFF22C55E),
            label = "solved",
        )
    }
    val shape = RoundedCornerShape(4.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(style.bg, shape)
            .border(0.5.dp, style.border, shape)
            .padding(horizontal = 7.dp, vertical = 2.dp),
    ) {
        Text(
            text = style.label,
            style = MaterialTheme.typography.labelSmall,
            color = style.fg,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UpsolveCardPreview() {
    ProblemBuddyTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                UpsolveCard("Palindrome Graph", "CF 1900 · Div 1 D", UpsolveBadge.Unattempted)
                UpsolveCard("XOR Partition", "CF 2100 · Div 1 E", UpsolveBadge.Penalty)
                UpsolveCard("Some Problem", "CF 1700 · Div 2 C", UpsolveBadge.Solved)
            }
        }
    }
}
