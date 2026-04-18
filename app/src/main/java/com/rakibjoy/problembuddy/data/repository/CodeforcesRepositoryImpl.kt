package com.rakibjoy.problembuddy.data.repository

import com.rakibjoy.problembuddy.core.database.dao.CachedPayloadDao
import com.rakibjoy.problembuddy.core.database.entity.CachedPayloadEntity
import com.rakibjoy.problembuddy.core.network.CodeforcesApi
import com.rakibjoy.problembuddy.core.network.dto.CfEnvelope
import com.rakibjoy.problembuddy.data.cache.SubmissionPersisted
import com.rakibjoy.problembuddy.data.cache.UserInfoPersisted
import com.rakibjoy.problembuddy.data.cache.toDomain
import com.rakibjoy.problembuddy.data.cache.toPersisted
import com.rakibjoy.problembuddy.data.mapper.toDomain
import com.rakibjoy.problembuddy.data.mapper.toUpcoming
import com.rakibjoy.problembuddy.domain.model.CodeforcesException
import com.rakibjoy.problembuddy.domain.model.Fresh
import com.rakibjoy.problembuddy.domain.model.RatingChange
import com.rakibjoy.problembuddy.domain.model.Submission
import com.rakibjoy.problembuddy.domain.model.UpcomingContest
import com.rakibjoy.problembuddy.domain.model.UserInfo
import com.rakibjoy.problembuddy.domain.repository.CodeforcesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodeforcesRepositoryImpl @Inject constructor(
    private val api: CodeforcesApi,
    private val cachedPayloadDao: CachedPayloadDao,
    private val json: Json,
) : CodeforcesRepository {

    private data class CacheEntry<T>(val value: T, val expiresAt: Long)

    private val cache = ConcurrentHashMap<String, CacheEntry<*>>()

    private val defaultTtlMs: Long = 5 * 60 * 1000L

    @Suppress("UNCHECKED_CAST")
    private fun <T> getCached(key: String): T? {
        val entry = cache[key] ?: return null
        if (System.currentTimeMillis() > entry.expiresAt) {
            cache.remove(key)
            return null
        }
        return entry.value as T
    }

    private fun <T> putCached(key: String, value: T, ttlMs: Long = defaultTtlMs) {
        cache[key] = CacheEntry(value, System.currentTimeMillis() + ttlMs)
    }

    override suspend fun userInfo(handle: String): Result<UserInfo> =
        fetch("userInfo:$handle") {
            val env = api.userInfo(handle)
            val dto = env.resolveOk().firstOrNull()
                ?: throw CodeforcesException.HandleNotFound()
            dto.toDomain()
        }

    override suspend fun userRating(handle: String): Result<List<RatingChange>> =
        fetch("userRating:$handle") {
            api.userRating(handle).resolveOk().map { it.toDomain() }
        }

    override suspend fun upcomingContests(): Result<List<UpcomingContest>> =
        fetch("upcomingContests", ttlMs = 15 * 60 * 1000L) {
            api.contestList(gym = false).resolveOk().mapNotNull { it.toUpcoming() }
        }

    override suspend fun userStatus(
        handle: String,
        from: Int,
        count: Int,
    ): Result<List<Submission>> =
        fetch("userStatus:$handle:$from:$count") {
            api.userStatus(handle, from, count).resolveOk().map { it.toDomain() }
        }

    override suspend fun userInfoWithFallback(handle: String): Result<Fresh<UserInfo>> {
        val key = "userInfo:$handle"
        val live = userInfo(handle)
        return live.fold(
            onSuccess = { info ->
                val now = System.currentTimeMillis()
                persistPayload(key, json.encodeToString(UserInfoPersisted.serializer(), info.toPersisted()), now)
                Result.success(Fresh(info, stale = false, fetchedAt = now))
            },
            onFailure = { err ->
                val cached = loadCachedUserInfo(key)
                if (cached != null) {
                    Result.success(Fresh(cached.first, stale = true, fetchedAt = cached.second))
                } else {
                    Result.failure(err)
                }
            },
        )
    }

    override suspend fun userStatusWithFallback(
        handle: String,
        from: Int,
        count: Int,
    ): Result<Fresh<List<Submission>>> {
        val key = "userStatus:$handle:$from:$count"
        val live = userStatus(handle, from, count)
        return live.fold(
            onSuccess = { subs ->
                val now = System.currentTimeMillis()
                val persisted = subs.map { it.toPersisted() }
                persistPayload(
                    key,
                    json.encodeToString(ListSerializer(SubmissionPersisted.serializer()), persisted),
                    now,
                )
                Result.success(Fresh(subs, stale = false, fetchedAt = now))
            },
            onFailure = { err ->
                val cached = loadCachedSubmissions(key)
                if (cached != null) {
                    Result.success(Fresh(cached.first, stale = true, fetchedAt = cached.second))
                } else {
                    Result.failure(err)
                }
            },
        )
    }

    private suspend fun persistPayload(key: String, payloadJson: String, fetchedAt: Long) {
        withContext(Dispatchers.IO) {
            runCatching {
                cachedPayloadDao.upsert(
                    CachedPayloadEntity(
                        cacheKey = key,
                        payloadJson = payloadJson,
                        fetchedAt = fetchedAt,
                    ),
                )
            }
        }
    }

    private suspend fun loadCachedUserInfo(key: String): Pair<UserInfo, Long>? =
        withContext(Dispatchers.IO) {
            val entity = runCatching { cachedPayloadDao.get(key) }.getOrNull() ?: return@withContext null
            runCatching {
                json.decodeFromString(UserInfoPersisted.serializer(), entity.payloadJson).toDomain()
            }.getOrNull()?.let { it to entity.fetchedAt }
        }

    private suspend fun loadCachedSubmissions(key: String): Pair<List<Submission>, Long>? =
        withContext(Dispatchers.IO) {
            val entity = runCatching { cachedPayloadDao.get(key) }.getOrNull() ?: return@withContext null
            runCatching {
                json.decodeFromString(
                    ListSerializer(SubmissionPersisted.serializer()),
                    entity.payloadJson,
                ).map { it.toDomain() }
            }.getOrNull()?.let { it to entity.fetchedAt }
        }

    private suspend fun <T> fetch(
        cacheKey: String,
        ttlMs: Long = defaultTtlMs,
        block: suspend () -> T,
    ): Result<T> {
        getCached<T>(cacheKey)?.let { return Result.success(it) }
        return withContext(Dispatchers.IO) {
            try {
                val value = block()
                putCached(cacheKey, value, ttlMs)
                Result.success(value)
            } catch (e: CodeforcesException) {
                Result.failure(e)
            } catch (e: HttpException) {
                val mapped = when (e.code()) {
                    429, 503 -> CodeforcesException.RateLimited()
                    else -> CodeforcesException.CodeforcesUnavailable(
                        e.message() ?: "HTTP ${e.code()}"
                    )
                }
                Result.failure(mapped)
            } catch (e: IOException) {
                Result.failure(CodeforcesException.CodeforcesUnavailable(e.message ?: "Network error"))
            } catch (e: Exception) {
                Result.failure(CodeforcesException.CodeforcesUnavailable(e.message ?: "Unknown error"))
            }
        }
    }

    private fun <T> CfEnvelope<T>.resolveOk(): T {
        if (status == "OK") {
            return result ?: throw CodeforcesException.CodeforcesUnavailable("Empty result")
        }
        val comment = comment.orEmpty()
        throw if (comment.contains("not found", ignoreCase = true)) {
            CodeforcesException.HandleNotFound(comment)
        } else {
            CodeforcesException.CodeforcesUnavailable(comment.ifEmpty { "Codeforces FAILED" })
        }
    }
}
