package com.rakibjoy.problembuddy.core.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rakibjoy.problembuddy.domain.model.Problem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyProblemNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun showDailyProblem(problem: Problem) {
        ensureChannel()
        val url = "https://codeforces.com/problemset/problem/${problem.contestId}/${problem.problemIndex}"
        val requestCode = problem.contestId * 1000 + problem.problemIndex.hashCode()
        val pending = PendingIntent.getActivity(
            context,
            requestCode,
            Intent(Intent.ACTION_VIEW, Uri.parse(url)),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val displayName = problem.name.ifBlank { "${problem.contestId}${problem.problemIndex}" }
        val ratingText = problem.rating?.toString() ?: "\u2014"
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("today's problem")
            .setContentText("$displayName \u00B7 $ratingText")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIF_ID, notif)
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = NotificationManagerCompat.from(context)
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Daily problem",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Daily Codeforces problem reminder"
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "daily_problem"
        private const val NOTIF_ID = 200
    }
}
