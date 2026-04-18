package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras

/**
 * Thin underlined tab row. Labels render lowercase-uppercased at 10sp
 * with wider letter-spacing. Underline sits under the selected tab only.
 */
@Composable
fun TabsRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = MaterialTheme.appExtras
    val violet = extras.accentVioletSoft

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 0.dp, // we draw the hairline via a Box spacer below
                color = androidx.compose.ui.graphics.Color.Transparent,
            ),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { i, label ->
                val selected = i == selectedIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelect(i) }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    Text(
                        text = label.uppercase(),
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp, // 0.05em @ 10sp
                        color = if (selected) violet else extras.textTertiary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(if (selected) 24.dp else 0.dp)
                            .background(if (selected) violet else androidx.compose.ui.graphics.Color.Transparent),
                    )
                }
            }
        }
        // Bottom hairline under all tabs.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(extras.borderSubtle),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TabsRowPreview() {
    ProblemBuddyTheme {
        Surface {
            TabsRow(
                tabs = listOf("tier ladder", "weak tags", "activity"),
                selectedIndex = 0,
                onSelect = {},
            )
        }
    }
}
