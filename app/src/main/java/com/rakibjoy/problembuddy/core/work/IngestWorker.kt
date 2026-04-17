package com.rakibjoy.problembuddy.core.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.rakibjoy.problembuddy.domain.model.IngestProgress
import com.rakibjoy.problembuddy.domain.model.TrainingJob
import com.rakibjoy.problembuddy.domain.repository.TrainingJobRepository
import com.rakibjoy.problembuddy.domain.usecase.IngestHandleUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect

@HiltWorker
class IngestWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val useCase: IngestHandleUseCase,
    private val trainingJobRepo: TrainingJobRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val raw = inputData.getString(KEY_HANDLES).orEmpty()
        val handles = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (handles.isEmpty()) return Result.failure()

        setForeground(createForegroundInfo("Starting training for ${handles.first()}"))

        var currentJobId = 0L
        currentJobId = trainingJobRepo.upsert(
            TrainingJob(
                id = currentJobId,
                handle = handles.first(),
                status = TrainingJob.Status.RUNNING,
                currentTier = null,
                done = 0,
                total = 0,
                error = null,
                updatedAt = System.currentTimeMillis(),
            ),
        )

        return try {
            var failed = false

            useCase(handles).collect { progress ->
                when (progress.phase) {
                    IngestProgress.Phase.FETCHING_SUBMISSIONS -> {
                        setForeground(createForegroundInfo("Fetching submissions for ${progress.handle}"))
                        currentJobId = trainingJobRepo.upsert(
                            TrainingJob(
                                id = currentJobId,
                                handle = progress.handle,
                                status = TrainingJob.Status.RUNNING,
                                currentTier = progress.tier,
                                done = progress.done,
                                total = progress.total,
                                error = null,
                                updatedAt = System.currentTimeMillis(),
                            ),
                        )
                    }

                    IngestProgress.Phase.WRITING_CORPUS -> {
                        val tierLabel = progress.tier?.label ?: "corpus"
                        setForeground(
                            createForegroundInfo(
                                "Writing $tierLabel (${progress.done}/${progress.total})",
                            ),
                        )
                        currentJobId = trainingJobRepo.upsert(
                            TrainingJob(
                                id = currentJobId,
                                handle = progress.handle,
                                status = TrainingJob.Status.RUNNING,
                                currentTier = progress.tier,
                                done = progress.done,
                                total = progress.total,
                                error = null,
                                updatedAt = System.currentTimeMillis(),
                            ),
                        )
                    }

                    IngestProgress.Phase.FAILED -> {
                        currentJobId = trainingJobRepo.upsert(
                            TrainingJob(
                                id = currentJobId,
                                handle = progress.handle,
                                status = TrainingJob.Status.FAILED,
                                currentTier = progress.tier,
                                done = progress.done,
                                total = progress.total,
                                error = "Failed to fetch submissions for ${progress.handle}",
                                updatedAt = System.currentTimeMillis(),
                            ),
                        )
                        failed = true
                    }

                    IngestProgress.Phase.COMPLETED -> {
                        currentJobId = trainingJobRepo.upsert(
                            TrainingJob(
                                id = currentJobId,
                                handle = progress.handle,
                                status = TrainingJob.Status.SUCCESS,
                                currentTier = progress.tier,
                                done = progress.total,
                                total = progress.total,
                                error = null,
                                updatedAt = System.currentTimeMillis(),
                            ),
                        )
                        // success; loop ends naturally
                    }
                }
            }

            when {
                failed -> Result.failure()
                else -> Result.success()
            }
        } catch (t: Throwable) {
            trainingJobRepo.upsert(
                TrainingJob(
                    id = currentJobId,
                    handle = handles.first(),
                    status = TrainingJob.Status.FAILED,
                    currentTier = null,
                    done = 0,
                    total = 0,
                    error = t.message ?: t.javaClass.simpleName,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            Result.failure()
        }
    }

    private fun createForegroundInfo(text: String): ForegroundInfo {
        ensureChannel()
        val notification: Notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("ProblemBuddy training")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = NotificationManagerCompat.from(applicationContext)
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Ingest",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Progress for corpus ingestion"
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        const val KEY_HANDLES = "handles"
        private const val CHANNEL_ID = "ingest"
        private const val NOTIFICATION_ID = 4242
    }
}
