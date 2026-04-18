package com.rakibjoy.problembuddy.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.rakibjoy.problembuddy.core.ui.theme.ProblemBuddyTheme
import com.rakibjoy.problembuddy.core.ui.theme.appExtras

/**
 * Lockup wordmark: `problem` in onSurface + `buddy` in violet soft.
 * Monospace, SemiBold, 15sp, tight negative tracking.
 */
@Composable
fun Wordmark(modifier: Modifier = Modifier) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val violet = MaterialTheme.appExtras.accentVioletSoft
    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = onSurface)) { append("problem") }
        withStyle(SpanStyle(color = violet)) { append("buddy") }
    }
    Text(
        text = text,
        modifier = modifier,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        letterSpacing = (-0.45).sp, // ≈ -0.03em @ 15sp
    )
}

@Preview
@Composable
private fun WordmarkPreview() {
    ProblemBuddyTheme {
        Surface { Wordmark() }
    }
}
