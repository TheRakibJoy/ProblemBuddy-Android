package com.rakibjoy.problembuddy.core.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IngestScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun enqueue(handles: List<String>) {
        val request = OneTimeWorkRequestBuilder<IngestWorker>()
            .setInputData(workDataOf(IngestWorker.KEY_HANDLES to handles.joinToString(",")))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.KEEP, request)
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME)
    }

    companion object {
        private const val UNIQUE_NAME = "ingest"
    }
}
