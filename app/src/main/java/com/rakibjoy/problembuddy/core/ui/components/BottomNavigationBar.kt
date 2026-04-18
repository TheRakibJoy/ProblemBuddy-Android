package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras

/** Primary app tabs surfaced in the bottom nav. */
enum class NavDestination(val label: String, val icon: ImageVector) {
    Home("home", Icons.Outlined.Home),
    Recommend("recommend", Icons.Filled.Search),
    Train("train", Icons.Filled.Bolt),
    Profile("profile", Icons.Filled.Person),
}

/**
 * Four-item bottom bar with 9sp lowercase labels. Violet soft highlight on
 * the selected item; tertiary text otherwise. Top hairline divider.
 * Pass `selected = null` to render the bar with no highlighted tab (used on
 * screens like Onboarding / Settings that are not part of the main tab set).
 */
@Composable
fun AppBottomBar(
    selected: NavDestination?,
    onSelect: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val extras = MaterialTheme.appExtras
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(extras.borderSubtle),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
        ) {
            NavDestination.entries.forEach { dest ->
                val isSelected = dest == selected
                val baseTint = if (isSelected) extras.accentVioletSoft else extras.textTertiary
                val tint = if (enabled) baseTint else baseTint.copy(alpha = 0.35f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (enabled) Modifier.clickable { onSelect(dest) } else Modifier,
                        )
                        .padding(vertical = 4.dp),
                ) {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label,
                        tint = tint,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = dest.label,
                        fontSize = 9.sp,
                        letterSpacing = 0.36.sp, // 0.04em @ 9sp
                        color = tint,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppBottomBarPreview() {
    ProblemBuddyTheme {
        AppBottomBar(selected = NavDestination.Home, onSelect = {})
    }
}
