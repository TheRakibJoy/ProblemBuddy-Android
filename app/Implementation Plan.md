# ProblemBuddy Android — Build Guide

This file guides any contributor (human or agent) building the on-device
Android port of ProblemBuddy. Follow it top to bottom: it encodes both **the
architecture** and **the order of work**.

The companion Django/React project lives in the repo root and is the source
of truth for algorithms (weak-tag detection, tier mapping, cosine ranking).
Port the logic, not the code — Python stays in `../`, Kotlin starts here.

---

## 1. Product brief

A native Kotlin app that helps Codeforces users pick their next problem by
identifying weak topics and recommending problems from a reference corpus.
**No backend**: every feature runs on the device.

Core flows (match the web app):

1. **Onboarding**: user types their Codeforces handle; we validate it via
   `GET /api/user.info`.
2. **Train**: user seeds the on-device problem corpus by ingesting
   submissions of strong handles (`tourist`, `Um_nik`, …). Shows a corpus
   overview (total problems, tags, handles, by-difficulty histogram) and a
   list of previously-trained handles with re-sync / remove actions.
3. **Recommend**: given the user's solved set and the corpus, compute weak
   tags and return unsolved problems ranked by cosine similarity of tags.
   Enforces a difficulty window `[rating + offset − 100, rating + offset + 200]`
   by default; swipe right to open, swipe left to skip.
4. **Profile**: hero + 4 tabs — tier ladder, weak tags, activity (heatmap,
   rating timeline, tag radar, verdict bar, failed-problem queue, milestones,
   tier projection, 1-year-ago snapshot), compare-with-handle.
5. **Home**: dashboard — upcoming contest countdown (`contest.list`),
   streak-at-risk banner, rating/solved/streak stats, next-tier progress
   pill, weekly-goal progress, problem of the day, today's picks, due-for-
   review list, upsolve queue, rotating tip.
6. **Settings**: appearance, recommendations, goals, notifications (daily
   problem reminder with hour + minute picker), data ops, about.

**Spaced-repetition review queue**: when a user marks a problem solved, it
enters a Leitner review schedule (boxes 0–5 with intervals 1/3/7/14/30/90
days). Problems whose `nextReviewAt` is due surface on Home.

**Daily problem**: one problem per (date, handle), cached via DataStore,
shown on Home and optionally fired as a notification via `WorkManager` at a
user-chosen local time.

**Incremental submission sync**: `user.status` is pulled in pages of 100
using a per-handle `lastSubmissionId` checkpoint; only new submissions are
merged with the cached list.

See the Python implementations for exact algorithms:

- Tier thresholds → `Dataset/constants.py::RATING_TIERS`
- Weak tag calculation → `Recommender/weak_tags.py`
- Recommender ranking → `Recommender/problem_giver.py::recommend`
- Corpus ingestion → `Dataset/add_data.py::ingest_handle`

---

## 2. Stack (locked-in choices)

| Concern | Choice | Reason |
| --- | --- | --- |
| Language | **Kotlin 2.0+** | Modern, null-safe, Compose-native |
| UI | **Jetpack Compose + Material 3** | Declarative, theme support, active ecosystem |
| DI | **Hilt** | Official, compile-time safe, pairs with Compose |
| Local DB | **Room** + KSP | SQLite with compile-time query validation |
| Preferences | **DataStore (Preferences)** | Structured, coroutine-native |
| HTTP | **Retrofit + OkHttp + kotlinx.serialization** | Idiomatic, minimal footprint |
| Images | **Coil** (Compose integration) | Avatar loading from Codeforces `titlePhoto` |
| Async | **Coroutines + Flow** | Structured concurrency baked into every layer |
| Navigation | **androidx.navigation.compose** (type-safe) | Standard, integrates with Hilt |
| Telemetry | **Firebase Analytics + Crashlytics** | Analytics always on; Crashlytics release-only |
| Testing | **JUnit 5 + MockK + Turbine** (unit) + **Compose UI Test** + **Room `MigrationTestHelper`** | Low ceremony, coroutine-aware |

**Avoid** unless you hit a concrete need: Dagger (use Hilt), RxJava, LiveData
(use Flow), Moshi (use kotlinx.serialization), MockK + Mockito mixing,
Gradle Groovy (use Kotlin DSL).

**Typography** ships with `FontFamily.Monospace` (system) so no network is
required for first paint; Google Fonts may be wired back in later if needed.

---

## 3. Architecture — MVI + Clean

Three layers, unidirectional data flow. MVI lives in the **UI layer**;
domain and data are plain Kotlin + Coroutines.

```
┌────────────────────────────── UI ──────────────────────────────┐
│  Composable screen                                             │
│        │  collectAsStateWithLifecycle()                        │
│        ▼                                                       │
│  StateFlow<ScreenState>  ◀──  reducer(state, intent) ─── ViewModel
│        │                                                 ▲    │
│        │                                                 │    │
│  Intent (sealed class) ──────────────────────────────────┘    │
│                                                                │
│  one-shot: Channel<ScreenEffect>  ──▶ side effects (toasts,   │
│                                        navigation, haptics)   │
└───────────────────────────── Domain ───────────────────────────┘
│  UseCase: suspend operator invoke(params): Result<T>           │
│  Pure models: Problem, Tier, WeakTag, TrainingJob              │
└───────────────────────────── Data ─────────────────────────────┘
│  Repository (interface in domain, impl in data)                │
│     ├── Room DAO (ProblemDao, CounterDao, InteractionDao…)     │
│     ├── CodeforcesApi (Retrofit)                               │
│     └── DataStore<Preferences>                                 │
└────────────────────────────────────────────────────────────────┘
```

**MVI contract** for every feature:

- **State**: single immutable `data class` describing everything the screen
  renders. No nullable container fields if a sealed class for status
  (`Loading | Success(data) | Error(message)`) is clearer.
- **Intent**: `sealed interface FooIntent` with `data class`/`data object`
  variants for every user action.
- **Effect**: one-shot events the UI can't re-render (navigation, toasts,
  share sheet). Use `Channel` + `receiveAsFlow()`, **not** StateFlow.
- **ViewModel**: exposes `val state: StateFlow<FooState>` and
  `fun onIntent(intent: FooIntent)`. Reducers are pure — side effects go
  through use cases or emit `FooEffect`.

Template (copy/adapt for each screen):

```kotlin
data class RecommendState(
    val loading: Boolean = true,
    val problems: List<Problem> = emptyList(),
    val filters: Filters = Filters(),
    val error: String? = null,
)

sealed interface RecommendIntent {
    data object Refresh : RecommendIntent
    data class UpdateFilters(val filters: Filters) : RecommendIntent
    data class MarkSolved(val problemId: Long) : RecommendIntent
}

sealed interface RecommendEffect {
    data class Toast(val message: String) : RecommendEffect
}

@HiltViewModel
class RecommendViewModel @Inject constructor(
    private val getRecommendations: GetRecommendationsUseCase,
    private val recordInteraction: RecordInteractionUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(RecommendState())
    val state: StateFlow<RecommendState> = _state.asStateFlow()

    private val _effects = Channel<RecommendEffect>(Channel.BUFFERED)
    val effects: Flow<RecommendEffect> = _effects.receiveAsFlow()

    init { onIntent(RecommendIntent.Refresh) }

    fun onIntent(intent: RecommendIntent) {
        when (intent) {
            RecommendIntent.Refresh -> refresh()
            is RecommendIntent.UpdateFilters -> {
                _state.update { it.copy(filters = intent.filters) }
                refresh()
            }
            is RecommendIntent.MarkSolved -> markSolved(intent.problemId)
        }
    }

    private fun refresh() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        getRecommendations(state.value.filters).fold(
            onSuccess = { problems -> _state.update { it.copy(loading = false, problems = problems) } },
            onFailure = { e ->
                _state.update { it.copy(loading = false, error = e.message) }
                _effects.send(RecommendEffect.Toast("Couldn't load: ${e.message}"))
            },
        )
    }
    // …
}
```

---

## 4. Module / package layout

Single-module app; packages organised feature-first.

```
app/
├── src/main/java/com/problembuddy/
│   ├── ProblemBuddyApp.kt           # @HiltAndroidApp
│   ├── MainActivity.kt
│   ├── Navigation.kt                 # NavHost + destinations
│   │
│   ├── core/
│   │   ├── network/                  # Retrofit, OkHttp, JSON config
│   │   │   ├── CodeforcesApi.kt
│   │   │   ├── NetworkModule.kt      # @Module
│   │   │   └── dto/                  # UserInfoDto, SubmissionDto, …
│   │   ├── database/
│   │   │   ├── ProblemBuddyDatabase.kt
│   │   │   ├── DatabaseModule.kt
│   │   │   ├── entity/               # ProblemEntity, CounterEntity, …
│   │   │   └── dao/
│   │   ├── datastore/
│   │   │   ├── SettingsStore.kt
│   │   │   └── DataStoreModule.kt
│   │   └── ui/                       # shared Composables (SkeletonCard, Pill)
│   │       ├── theme/
│   │       └── components/
│   │
│   ├── domain/                       # pure Kotlin — NO Android/Room/Retrofit imports
│   │   ├── model/                    # Problem, Counter, Tier, WeakTag, Filters
│   │   ├── repository/               # interfaces only
│   │   └── usecase/
│   │       ├── GetRecommendationsUseCase.kt
│   │       ├── ComputeWeakTagsUseCase.kt
│   │       ├── IngestHandleUseCase.kt
│   │       ├── RecordInteractionUseCase.kt
│   │       ├── ValidateCfHandleUseCase.kt
│   │       └── …
│   │
│   ├── data/
│   │   ├── repository/               # implementations
│   │   │   ├── ProblemRepositoryImpl.kt
│   │   │   ├── CodeforcesRepositoryImpl.kt
│   │   │   └── …
│   │   ├── mapper/                   # Entity ↔ Domain, Dto ↔ Domain
│   │   ├── recommender/              # cosine similarity engine (pure Kotlin)
│   │   │   └── TierIndex.kt
│   │   └── RepositoryModule.kt       # @Binds interface → impl
│   │
│   └── feature/
│       ├── onboarding/
│       │   ├── OnboardingScreen.kt
│       │   ├── OnboardingState.kt
│       │   ├── OnboardingIntent.kt
│       │   └── OnboardingViewModel.kt
│       ├── recommend/
│       ├── profile/
│       ├── train/
│       └── settings/
│
├── src/test/                         # unit tests (use cases, mappers, ranking)
└── src/androidTest/                  # Compose UI tests + Room migration tests
```

**Rule**: `domain/` never imports from `data/`, `core/network/`, or
`androidx.*`. It's pure Kotlin. Use cases return `Result<T>` or throw
typed domain exceptions (`CodeforcesUnavailable`, `HandleNotFound`, …).

---

## 5. Data model (Room entities mirror Django models)

```kotlin
@Entity(tableName = "problems",
        indices = [Index(value = ["tier", "rating"]),
                   Index(value = ["tier", "contestId", "problemIndex"], unique = true)])
data class ProblemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tier: String,            // "pupil" | "specialist" | … | "legendary_grandmaster"
    val contestId: Int,
    val problemIndex: String,    // "A", "B1", …
    val rating: Int?,
    val tags: String,            // comma-joined; split when needed
)

@Entity(tableName = "counters",
        indices = [Index(value = ["tagName", "tier"], unique = true)])
data class CounterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tagName: String,
    val tier: String,
    val count: Int,
)

@Entity(tableName = "handles")
data class HandleEntity(@PrimaryKey val handle: String)

@Entity(tableName = "interactions",
        indices = [Index(value = ["problemId"], unique = true)])
data class InteractionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val problemId: Long,
    val status: String,          // "solved" | "not_interested" | "hidden"
    val createdAt: Long,
)

@Entity(tableName = "training_jobs")
data class TrainingJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val handle: String,
    val status: String,          // "queued" | "running" | "success" | "failed"
    val currentTier: String,
    val done: Int,
    val total: Int,
    val error: String,
    val updatedAt: Long,
)
```

Tier constants — keep a single source in domain:

```kotlin
enum class Tier(val floor: Int, val target: Int, val label: String) {
    NEWBIE(0, 1200, "Newbie"),
    PUPIL(1200, 1400, "Pupil"),
    SPECIALIST(1400, 1600, "Specialist"),
    EXPERT(1600, 1900, "Expert"),
    CANDIDATE_MASTER(1900, 2100, "Candidate Master"),
    MASTER(2100, 2300, "Master"),
    INTL_MASTER(2300, 2400, "International Master"),
    GRANDMASTER(2400, 2600, "Grandmaster"),
    INTL_GRANDMASTER(2600, 3000, "International Grandmaster"),
    LEGENDARY(3000, 4000, "Legendary Grandmaster");

    companion object {
        fun forMaxRating(rating: Int): Tier =
            entries.lastOrNull { rating >= it.floor } ?: NEWBIE
    }
}
```

---

## 6. Recommender algorithm (pure Kotlin port)

No scikit-learn. Cosine similarity on bag-of-words tag vectors fits in
~40 lines. Build `TierIndex` once per (tier, corpus version), cache in
memory.

```kotlin
data class TierIndex(
    val problems: List<Problem>,
    val vocabulary: Map<String, Int>,     // tag -> column
    val vectors: Array<IntArray>,         // row per problem
) {
    fun rank(weakTags: List<String>): List<Int> {
        val query = IntArray(vocabulary.size)
        for (tag in weakTags) vocabulary[tag]?.let { query[it]++ }
        val qNorm = query.norm().takeIf { it > 0 } ?: return emptyList()
        return vectors.indices.sortedByDescending { i ->
            val row = vectors[i]
            val rNorm = row.norm()
            if (rNorm == 0.0) 0.0 else dot(query, row) / (qNorm * rNorm)
        }
    }
}

private fun IntArray.norm(): Double = sqrt(sumOf { (it * it).toDouble() })
private fun dot(a: IntArray, b: IntArray): Int {
    var s = 0; for (i in a.indices) s += a[i] * b[i]; return s
}
```

Invalidate the cached `TierIndex` whenever a training job writes new rows.

---

## 7. Implementation phases

Work sequentially. Every phase ends with "green tests + runnable app".

### Phase 0 — Scaffold (day 1)

- `gradle/libs.versions.toml` with all dependency versions pinned.
- Android Studio "Empty Compose Activity" template, then swap Groovy for
  Kotlin DSL (`build.gradle.kts`).
- Apply Hilt, KSP, Room, kotlinx.serialization plugins in the module.
- Add `ProblemBuddyApp : Application` with `@HiltAndroidApp`.
- Commit: "chore: project scaffold".

### Phase 1 — Core infrastructure (days 2–3)

- Room database + DAOs for every entity above (no migrations yet; v1).
- `CodeforcesApi` Retrofit interface with `userInfo`, `userRating`,
  `userStatus`. Base URL `https://codeforces.com/api/`.
- `CodeforcesRepository` wrapping the API with:
    - in-memory cache (ConcurrentHashMap with TTL) keyed by endpoint + params
    - typed errors (`CodeforcesUnavailable`, `HandleNotFound`)
- `SettingsStore` (DataStore Preferences) for theme, cfHandle, recsPerLoad,
  difficultyOffset.
- Seed `Tier` enum in domain; unit-test `forMaxRating` with every boundary
  (same parametrised cases as `tests/test_target.py`).

### Phase 2 — Ingest engine + corpus (days 4–5)

- `IngestHandleUseCase` — port of `ingest_handle` + `ingest_all_tiers`.
  Writes `ProblemEntity` / `CounterEntity`, reports progress via a
  `Flow<IngestProgress>`.
- `TrainingJobRepository` persists running jobs and exposes a
  `Flow<TrainingJob?>` for the active one.
- Background execution: `WorkManager` with a unique
  `OneTimeWorkRequest(IngestWorker::class)`. Survives process death, shows
  foreground notification with progress. Equivalent to the web app's
  in-thread ingestion.
- UI: TrainScreen (MVI) with a live-validated handle input, Start button,
  and progress card fed by `Flow<TrainingJob?>`. Mirror UX of
  `frontend/src/train/TrainingWatcher.tsx`.

### Phase 3 — Recommendation engine (day 6)

- `TierIndex` + cosine ranking (pure Kotlin, unit-testable).
- `GetRecommendationsUseCase(filters: Filters): Result<List<Problem>>`:
    1. Resolve user tier from cached Codeforces `userInfo`.
    2. Compute weak tags (`ComputeWeakTagsUseCase`).
    3. Rank tier's `ProblemEntity` rows via `TierIndex.rank`.
    4. Filter: solved set (from Codeforces `userStatus`), interactions,
       rating range, include/exclude tags, `weakOnly`.
    5. Take `filters.count`.
- Unit test with fixture corpus: rank deterministic, filters compose.

### Phase 4 — Screens (days 7–10)

Build in this order, one per day, each fully MVI:

1. **OnboardingScreen** — Text field + validator; writes `cfHandle` to
   DataStore; navigates to Home.
2. **HomeScreen** — Welcome + "Get problems" CTA → Recommend.
3. **RecommendScreen** — Skeleton shimmer row → problem cards with
   accent gradient (Compose `Brush.linearGradient` based on tag hash),
   reason chip, Solve CTA, Mark Solved / Skip buttons. Filter bottom
   sheet for tags / rating range / count.
4. **ProfileScreen** — Horizontal scroll of Tier pills (past / current /
   goal / future) + progress bars for weak tags.
5. **SettingsScreen** — Theme radio (System/Light/Dark), slider for
   `recsPerLoad`, slider for `difficultyOffset`, reset corpus, delete
   local data (with confirmation).
6. **TrainScreen** — (already built in Phase 2, polish here).

Each screen: `Screen.kt` composable takes `state` + `onIntent`; a
`Screen(viewModel: ScreenViewModel = hiltViewModel())` overload wires it
up. Previews use a hand-constructed state for instant rendering.

### Phase 5 — Theme + navigation polish (day 11)

- Dynamic color on Android 12+, fall back to hand-crafted scheme.
- `ThemePreference` read from DataStore; wrap `NavHost` in
  `ProblemBuddyTheme(isDark = effective)`.
- Type-safe navigation (Compose Navigation 2.8 routes are `@Serializable`
  objects). Define `Destination.kt` enum or sealed hierarchy.
- Bottom bar with Home / Recommend / Profile / Settings; Train in a
  top-app-bar overflow.

### Phase 6 — Persistence & resilience (day 12)

- Add Room export-schema on every `@Database` migration (put schemas in
  `app/schemas/` and assert them in `androidTest`).
- Offline handling: if Codeforces is unreachable, use cached `userInfo`
  and show a banner. Recommender falls back to "last known weak tags".
- Handle empty-corpus case with a clear "Run training first" empty state.

### Phase 7 — Tests & CI (days 13–14)

- **Unit** (`src/test`): tier math, weak-tag computation, cosine ranking,
  mappers (codeforces / problem / training-job), handle validation,
  recommendation end-to-end.
- **Compose** (`src/androidTest`): `RecommendScreenTest` — skeleton state,
  cards render, filter icon emits intent, overflow "Mark solved" removes card.
- **Room migration** (`src/androidTest`): `ProblemBuddyDatabaseMigrationTest`
  asserts v1 → v2 preserves rows and adds `cached_payloads`.
- GitHub Actions: two workflows.
  1. `android.yml` — assemble debug + unit tests + instrumentation-compile on
     every push/PR; connected tests in an emulator matrix over API 29 & 34
     via `reactivecircus/android-emulator-runner` with AVD caching.
  2. `publish-apk.yml` — release APK pushed to an orphan `downloads` branch
     as `ProblemBuddy-latest.apk` + sha-pinned name + README.

### Phase 8 — Release prep (day 15)

- Release signed with the debug keystore for side-loading (no store
  distribution). If you later want Play-Store signing, add a `signingConfig`
  driven by env vars / GitHub Secrets.
- R8 enabled; `proguard-rules.pro` ships keeps for kotlinx.serialization
  DTOs, Retrofit service interfaces (R8 full-mode recipe), `IngestWorker`
  and `DailyProblemWorker`.
- `./gradlew assembleRelease` → APK ready for GitHub `downloads` branch.

### Phase 9 — Activity history & daily engagement (post-MVP)

- **Daily problem of the day** (`GetTodayProblemUseCase`): deterministic per
  (date, handle), cached in DataStore, surfaced on Home and optionally via
  notification.
- **Daily notification worker** (`DailyProblemWorker` + `DailyProblemScheduler`):
  PeriodicWorkRequest, user-chosen hour + minute (TimePicker), channel
  `daily_problem`, POST_NOTIFICATIONS permission handshake on Android 13+.
- **Spaced-repetition review queue** (`ReviewEntity` + `ReviewRepository`):
  Leitner boxes 0–5 with intervals 1/3/7/14/30/90 days, scheduled on
  MarkSolved, surfaced on Home.
- **Activity analytics** (`ProfileViewModel.buildActivityStats`): heatmap,
  streak/longest/this-year, rating timeline, recent contests, division deltas,
  tier stacked area, tag radar (8 canonical tags), first-attempt AC rate,
  verdict breakdown, failed queue, day-of-week + hour-of-day, language mix,
  rated vs virtual counts, milestones, tier projection, 1-year-ago snapshot.
- **Upcoming contest countdown** (`CodeforcesApi.contestList`, 15-min TTL):
  `UpcomingContestCard` with live countdown + register action.
- **Incremental sync**: per-handle checkpoint in DataStore; pages of 100 merged
  against the persistent cache.
- **Compare with another handle**: profile tab with text field + side-by-side
  rating/tier card.
- **Home dashboard additions**: streak-at-risk banner, next-tier progress
  pill, weekly-goal card.

### Phase 10 — Firebase integration (post-MVP)

- `google-services.json` in `app/`, Google Services + Crashlytics Gradle
  plugins.
- `Firebase.analytics.setAnalyticsCollectionEnabled(true)` on app start —
  both debug and release.
- `Firebase.crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG`
  so local crashes don't pollute the production dashboard.

---

## 8. Non-negotiables / guardrails

1. **Never import Android or framework types in `domain/`.** Pure Kotlin.
2. **Never mutate state outside the ViewModel reducer.** `_state.update {}`
   only.
3. **Never block the main thread.** All IO/DB/network is `suspend` and
   dispatched to `Dispatchers.IO` at the repository boundary.
4. **Never store secrets in git.** No signing keys, no `local.properties`.
5. **Single source of truth for tiers.** `domain.model.Tier` only; never
   hard-code rating thresholds elsewhere.
6. **One screen = one ViewModel.** Don't share ViewModels across screens —
   use shared repositories instead.
7. **Test the reducer, not the coroutine.** Push async work into use cases
   and inject fakes in ViewModel tests.

---

## 9. Parity with the web app

When in doubt about an algorithm or UX, consult the web implementation:

| Web concern | Android port reference |
| --- | --- |
| `Dataset/constants.py::RATING_TIERS` | `domain.model.Tier` (CF-accurate bands + colors) |
| `Dataset/codeforces.py::user_info` | `CodeforcesApi.userInfo` + repo cache + `Fresh<T>` offline fallback |
| `Dataset/add_data.py::ingest_handle` | `IngestHandleUseCase` + `IngestWorker` |
| `Recommender/problem_giver.py::recommend` | `GetRecommendationsUseCase` + `TierIndex` (+ difficulty window) |
| `Recommender/weak_tags.py::get_weak_tags` | `ComputeWeakTagsUseCase` |
| `frontend/src/recommend/RecommendPage.tsx` | `feature.recommend.RecommendScreen` (swipe-able) |
| `frontend/src/profile/TierLadder.tsx` | `feature.profile.ProfileScreen` + `VerticalTierLadder` |
| `frontend/src/train/TrainingWatcher.tsx` | `feature.train.TrainScreen` (corpus overview + history) |

Features that don't have a web equivalent (Android-original):

- Spaced-repetition review queue (`ReviewRepository`, Leitner boxes)
- Daily problem notification (`DailyProblemWorker`)
- Incremental `user.status` sync with per-handle checkpoint
- Activity analytics suite (`ActivityStats` + 15+ derived views)
- Upcoming-contest countdown + Home dashboard (streak risk, next-tier pill)
- Compare-with-handle on Profile

If you add a new algorithm here, consider porting back to Python so both
clients stay aligned.

---

## 10. Starter checklist for the first PR

- [ ] `build.gradle.kts` (app + root) with Kotlin DSL.
- [ ] `libs.versions.toml` with Kotlin, AGP, Compose BOM, Hilt, Room,
  Retrofit, DataStore, kotlinx-serialization, WorkManager,
  androidx.navigation.compose, Turbine, MockK, JUnit 5.
- [ ] `ProblemBuddyApp : Application` + `@HiltAndroidApp`.
- [ ] Empty `MainActivity` hosting a `NavHost` with a single "Hello"
  screen.
- [ ] Room database wired with one placeholder DAO to prove
  KSP + Hilt work.
- [ ] `./gradlew assembleDebug testDebugUnitTest` passes.
- [ ] GitHub Actions workflow file that runs the same commands on push.

Once this lands, everything else slots into the feature packages above.
