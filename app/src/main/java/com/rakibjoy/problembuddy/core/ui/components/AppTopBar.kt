package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme

/**
 * Top bar rendering the [Wordmark] on the left with optional right-side actions.
 * Transparent background so it composes on [GradientSurface] or a plain surface.
 * Padding: 14dp horizontal / 10dp vertical per the redesign mockup.
 */
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Wordmark()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = actions,
        )
    }
}

/**
 * Back-compat overload for call sites that still pass a `title`. The title is
 * intentionally ignored — the redesign always shows the [Wordmark] lockup.
 * Navigation icons from the old API are prepended before the wordmark.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    if (navigationIcon != null) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                navigationIcon()
                Wordmark()
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                content = actions,
            )
        }
    } else {
        AppTopBar(modifier = modifier, actions = actions)
    }
}

@Preview
@Composable
private fun AppTopBarPreview() {
    ProblemBuddyTheme {
        Surface(color = Color.Transparent) {
            AppTopBar()
        }
    }
}
