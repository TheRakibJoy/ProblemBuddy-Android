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
   submissions of strong handles (`tourist`, `Um_nik`, …). Runs once; can
   be re-run to refresh.
3. **Recommend**: given the user's solved set and the corpus, compute weak
   tags and return unsolved problems ranked by cosine similarity of tags.
4. **Profile**: tier ladder (Pupil → Legendary GM) + weak-tag bars.
5. **Settings**: theme, recs per load, difficulty offset, reset corpus.

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
| Async | **Coroutines + Flow** | Structured concurrency baked into every layer |
| Navigation | **androidx.navigation.compose** (type-safe) | Standard, integrates with Hilt |
| Testing | **JUnit 5 + MockK + Turbine** (unit) + **Compose UI Test** (screen) | Low ceremony, coroutine-aware |

**Avoid** unless you hit a concrete need: Dagger (use Hilt), RxJava, LiveData
(use Flow), Moshi (use kotlinx.serialization), MockK + Mockito mixing,
Gradle Groovy (use Kotlin DSL).

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
    PUPIL(0, 1200, "Pupil"),
    SPECIALIST(1200, 1400, "Specialist"),
    EXPERT(1400, 1600, "Expert"),
    CANDIDATE_MASTER(1600, 1900, "Candidate Master"),
    MASTER(1900, 2100, "Master"),
    INTL_MASTER(2100, 2300, "International Master"),
    GRANDMASTER(2300, 2400, "Grandmaster"),
    INTL_GRANDMASTER(2400, 2600, "International Grandmaster"),
    LEGENDARY(2600, 3000, "Legendary Grandmaster");

    companion object {
        fun forMaxRating(rating: Int): Tier =
            entries.lastOrNull { rating >= it.floor } ?: PUPIL
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

- **Unit** (`src/test`): tier math, weak-tag computation, ranking, all
  mappers, handle validation.
- **Compose** (`src/androidTest`): RecommendScreen renders skeletons then
  cards; filter change triggers refresh; MarkSolved removes a card.
- **Room migration** tests once you have a v2.
- GitHub Actions: matrix (api-level 29 + 34) with
  `./gradlew testDebugUnitTest connectedDebugAndroidTest`.
  Use Google Reactive's emulator runner or KSP-only unit slice to keep
  CI under 10 minutes.

### Phase 8 — Release prep (day 15)

- Signing config from environment variables (no keystores in git).
- R8 enabled; confirm Compose keeps + Retrofit/Hilt keeps in
  `proguard-rules.pro`.
- `./gradlew bundleRelease` → AAB ready for Play Console upload.

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
| `Dataset/constants.py::RATING_TIERS` | `domain.model.Tier` |
| `Dataset/codeforces.py::user_info` | `CodeforcesApi.userInfo` + repo cache |
| `Dataset/add_data.py::ingest_handle` | `IngestHandleUseCase` |
| `Recommender/problem_giver.py::recommend` | `GetRecommendationsUseCase` + `TierIndex` |
| `Recommender/weak_tags.py::get_weak_tags` | `ComputeWeakTagsUseCase` |
| `frontend/src/recommend/RecommendPage.tsx` | `feature.recommend.RecommendScreen` |
| `frontend/src/profile/TierLadder.tsx` | `feature.profile.TierLadderSection` |
| `frontend/src/train/TrainingWatcher.tsx` | `feature.train.TrainProgressCard` |

If you add a new algorithm here first, port it back to Python too so both
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
