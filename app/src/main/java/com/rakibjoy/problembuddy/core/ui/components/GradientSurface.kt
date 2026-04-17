package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme

@Composable
fun GradientSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val brush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface,
        ),
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush),
    ) {
        content()
    }
}

@Preview(name = "GradientSurface", showBackground = true)
@Composable
private fun GradientSurfacePreview() {
    ProblemBuddyTheme {
        Surface {
            GradientSurface {
                Text(text = "Gradient background", modifier = Modifier)
            }
        }
    }
}
