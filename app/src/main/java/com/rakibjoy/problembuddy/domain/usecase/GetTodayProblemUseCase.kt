package com.rakibjoy.problembuddy.domain.usecase

import com.rakibjoy.problembuddy.core.datastore.SettingsStore
import com.rakibjoy.problembuddy.data.cache.ProblemPersisted
import com.rakibjoy.problembuddy.data.cache.toDomain
import com.rakibjoy.problembuddy.data.cache.toPersisted
import com.rakibjoy.problembuddy.domain.model.Filters
import com.rakibjoy.problembuddy.domain.model.Problem
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Returns the deterministic "problem of the day" for the current user.
 *
 * The picked problem is cached in DataStore keyed by ISO date so that the same
 * problem is returned all day across screen recompositions / app restarts.
 * On day rollover (or cache miss / decode failure) a fresh recommendation pass
 * runs with `weakOnly = true, count = 1` and the first problem is persisted.
 */
class GetTodayProblemUseCase @Inject constructor(
    private val settingsStore: SettingsStore,
    private val getRecommendations: GetRecommendationsUseCase,
    private val json: Json,
) {
    suspend operator fun invoke(): Problem? {
        val todayIso = LocalDate.now(ZoneId.systemDefault()).toString()

        val cachedDate = settingsStore.dailyProblemDate.first()
        val cachedJson = settingsStore.dailyProblemJson.first()
        if (cachedDate == todayIso && !cachedJson.isNullOrEmpty()) {
            runCatching {
                json.decodeFromString(ProblemPersisted.serializer(), cachedJson).toDomain()
            }.getOrNull()?.let { return it }
        }

        val problem = getRecommendations(Filters(count = 1, weakOnly = true))
            .getOrNull()
            ?.problems
            ?.firstOrNull()
            ?: return null

        val serialized = json.encodeToString(
            ProblemPersisted.serializer(),
            problem.toPersisted(),
        )
        settingsStore.setDailyProblem(todayIso, serialized)
        return problem
    }
}
