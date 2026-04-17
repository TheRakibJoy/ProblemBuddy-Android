package com.rakibjoy.problembuddy.domain.usecase

import com.rakibjoy.problembuddy.core.database.dao.CounterDao
import com.rakibjoy.problembuddy.core.database.dao.HandleDao
import com.rakibjoy.problembuddy.core.database.dao.ProblemDao
import com.rakibjoy.problembuddy.core.database.entity.CounterEntity
import com.rakibjoy.problembuddy.core.database.entity.HandleEntity
import com.rakibjoy.problembuddy.core.database.entity.ProblemEntity
import com.rakibjoy.problembuddy.domain.model.IngestProgress
import com.rakibjoy.problembuddy.domain.model.Submission
import com.rakibjoy.problembuddy.domain.model.Tier
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IngestHandleUseCase @Inject constructor(
    private val codeforces: CodeforcesRepository,
    private val problemDao: ProblemDao,
    private val counterDao: CounterDao,
    private val handleDao: HandleDao,
) {
    operator fun invoke(handles: List<String>): Flow<IngestProgress> = flow {
        if (handles.isEmpty()) return@flow

        for (handle in handles) {
            emit(
                IngestProgress(
                    handle = handle,
                    tier = null,
                    done = 0,
                    total = 0,
                    phase = IngestProgress.Phase.FETCHING_SUBMISSIONS,
                ),
            )

            val result = codeforces.userStatus(handle, from = 1, count = 100_000)
            val submissions = result.getOrNull()
            if (submissions == null) {
                emit(
                    IngestProgress(
                        handle = handle,
                        tier = null,
                        done = 0,
                        total = 0,
                        phase = IngestProgress.Phase.FAILED,
                    ),
                )
                continue
            }

            val accepted = submissions
                .asSequence()
                .filter { it.verdict == "OK" }
                .filter { it.problem.contestId != 0 }
                .filter { it.problem.problemIndex.isNotBlank() }
                .filter { it.problem.rating != null }
                .toList()

            val deduped: List<Submission> = accepted
                .associateBy { it.problem.contestId to it.problem.problemIndex }
                .values
                .toList()

            withContext(Dispatchers.IO) {
                handleDao.insert(HandleEntity(handle = handle))
            }

            val byTier: Map<Tier, List<Submission>> = deduped.groupBy { sub ->
                Tier.forMaxRating(sub.problem.rating!!)
            }

            for ((tier, group) in byTier) {
                val total = group.size
                emit(
                    IngestProgress(
                        handle = handle,
                        tier = tier,
                        done = 0,
                        total = total,
                        phase = IngestProgress.Phase.WRITING_CORPUS,
                    ),
                )

                val tierKey = tier.name.lowercase()

                val problems = group.map { sub ->
                    ProblemEntity(
                        tier = tierKey,
                        contestId = sub.problem.contestId,
                        problemIndex = sub.problem.problemIndex,
                        rating = sub.problem.rating,
                        tags = sub.problem.tags.joinToString(","),
                    )
                }

                // Count tag increments for this group.
                val increments: Map<String, Int> = buildMap {
                    for (sub in group) {
                        for (tag in sub.problem.tags) {
                            if (tag.isBlank()) continue
                            put(tag, (get(tag) ?: 0) + 1)
                        }
                    }
                }

                withContext(Dispatchers.IO) {
                    problemDao.insertAll(problems)

                    if (increments.isNotEmpty()) {
                        val current = counterDao.getByTier(tierKey)
                        val byTag = current.associateBy { it.tagName }
                        val merged = increments.map { (tag, delta) ->
                            val existing = byTag[tag]
                            if (existing != null) {
                                existing.copy(count = existing.count + delta)
                            } else {
                                CounterEntity(
                                    tagName = tag,
                                    tier = tierKey,
                                    count = delta,
                                )
                            }
                        }
                        counterDao.upsertAll(merged)
                    }
                }

                emit(
                    IngestProgress(
                        handle = handle,
                        tier = tier,
                        done = total,
                        total = total,
                        phase = IngestProgress.Phase.WRITING_CORPUS,
                    ),
                )
            }
        }

        emit(
            IngestProgress(
                handle = handles.last(),
                tier = null,
                done = 0,
                total = 0,
                phase = IngestProgress.Phase.COMPLETED,
            ),
        )
    }
}
