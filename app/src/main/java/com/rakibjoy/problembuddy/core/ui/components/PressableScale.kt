package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme

/**
 * Scales the composable down while pressed (tap-feedback). Pair with a
 * separate `Modifier.clickable { ... }` to handle the click itself — this
 * modifier only provides the visual press animation.
 */
fun Modifier.pressScale(scale: Float = 0.96f): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val animated by animateFloatAsState(
        targetValue = if (pressed) scale else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "press-scale",
    )
    this
        .scale(animated)
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val down = awaitPointerEvent()
                    if (down.changes.any { it.pressed }) {
                        pressed = true
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.none { it.pressed }) {
                                pressed = false
                                break
                            }
                        }
                    }
                }
            }
        }
}

@Preview(name = "PressableScale", showBackground = true)
@Composable
private fun PressableScalePreview() {
    ProblemBuddyTheme {
        Surface {
            Card(modifier = Modifier.pressScale().padding(16.dp)) {
                Text("Press me", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
