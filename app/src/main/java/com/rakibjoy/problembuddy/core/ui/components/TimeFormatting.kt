package com.rakibjoy.problembuddy.core.ui.components

import java.time.Duration
import java.time.Instant

/**
 * Format a unix-seconds timestamp as a short relative-time string, e.g. "2d ago",
 * "3h ago", "5m ago", "just now". Values in the future are displayed as "now".
 */
internal fun relativeTime(timeSeconds: Long, nowSeconds: Long = Instant.now().epochSecond): String {
    val diff = Duration.ofSeconds((nowSeconds - timeSeconds).coerceAtLeast(0L))
    val days = diff.toDays()
    val hours = diff.toHours()
    val minutes = diff.toMinutes()
    return when {
        days >= 365 -> "${days / 365}y ago"
        days >= 30 -> "${days / 30}mo ago"
        days >= 1 -> "${days}d ago"
        hours >= 1 -> "${hours}h ago"
        minutes >= 1 -> "${minutes}m ago"
        else -> "just now"
    }
}
