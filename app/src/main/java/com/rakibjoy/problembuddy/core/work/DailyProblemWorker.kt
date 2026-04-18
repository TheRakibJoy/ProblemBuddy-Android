package com.rakibjoy.problembuddy.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rakibjoy.problembuddy.domain.model.Filters
import com.rakibjoy.problembuddy.domain.usecase.GetRecommendationsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyProblemWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getRecommendations: GetRecommendationsUseCase,
    private val notifier: DailyProblemNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val problem = runCatching {
            getRecommendations(Filters(count = 1, weakOnly = true))
                .getOrNull()?.problems?.firstOrNull()
        }.getOrNull() ?: return Result.success()

        notifier.showDailyProblem(problem)
        return Result.success()
    }
}
