package com.rakibjoy.problembuddy.core.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyProblemScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** Schedule the daily problem notification for [hourOfDay]:[minuteOfHour] local time. */
    fun enqueue(hourOfDay: Int, minuteOfHour: Int = 0) {
        val hour = hourOfDay.coerceIn(0, 23)
        val minute = minuteOfHour.coerceIn(0, 59)
        val initialDelayMs = computeInitialDelayMs(hour, minute)
        val request = PeriodicWorkRequestBuilder<DailyProblemWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME)
    }

    private fun computeInitialDelayMs(hourOfDay: Int, minuteOfHour: Int): Long {
        val now = LocalDateTime.now()
        var next = now.withHour(hourOfDay).withMinute(minuteOfHour).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis()
    }

    companion object {
        private const val UNIQUE_NAME = "dailyProblem"
    }
}
