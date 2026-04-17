# ProblemBuddy Android

A native Kotlin Android app that helps Codeforces users pick their next problem. It
identifies your weak topics from your solved set and recommends problems from an
on-device corpus of strong handles' submissions — **no backend**, everything runs on
the device.

This is the Android port of the web app at
[TheRakibJoy/ProblemBuddy](https://github.com/TheRakibJoy/ProblemBuddy) (Django +
React). Same recommendation logic, now running entirely on your phone.

Idea and original project by **[RakibJoy](https://codeforces.com/profile/RakibJoy)**.

> Status: feature-complete through the build guide in
> [`app/Implementation Plan.md`](app/Implementation%20Plan.md).
> Release APKs are published automatically to the [`downloads`](../../tree/downloads)
> branch of this repo on every push to `main`.

## Download & install

1. On your Android phone, open the [`downloads`](../../tree/downloads) branch.
2. Tap `ProblemBuddy-latest.apk` → **Download**.
3. Open the APK. Android will prompt you to allow installs from your browser
   (one-time). Tap **Install**.

The APK is signed with a debug key, so Android will show an "unknown source"
warning. That's expected for direct side-loading and does not affect functionality.
Minimum supported OS is **Android 8.0 (API 26)**.

## Features

- **Onboarding**: enter your Codeforces handle; validated against
  `GET /api/user.info` before you can continue.
- **Train**: seed the on-device corpus by ingesting submissions of strong handles.
  Runs in a foreground `WorkManager` worker with a progress notification.
- **Recommend**: given your solved set and the corpus, the app computes your weak
  tags per tier and ranks unsolved problems by cosine similarity of tag vectors.
  Filter by rating range, include/exclude tags, and problems-per-load.
- **Profile**: tier ladder (Pupil → Legendary GM) plus weak-tag coverage bars.
- **Settings**: theme (System/Light/Dark), recommendations per load, difficulty
  offset, reset corpus, delete all data.
- **Offline resilience**: `userInfo` and `userStatus` responses are persisted in
  Room; if Codeforces is unreachable, the app falls back to cached data and shows
  a "Offline data" banner with the cache age.

## Stack

| Concern | Choice |
| --- | --- |
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt 2.52 (KSP) |
| Local DB | Room 2.6.1 + auto-migrations |
| Preferences | DataStore (Preferences) |
| Background work | WorkManager 2.9.1 + Hilt Worker |
| HTTP | Retrofit 2.11 + OkHttp 4.12 + kotlinx.serialization 1.7.3 |
| Async | Coroutines + Flow |
| Navigation | androidx.navigation.compose 2.8 (type-safe) |
| Testing | JUnit 5 (unit) + MockK + Turbine; Compose UI Test + Room `MigrationTestHelper` (instrumented) |
| Min / Target SDK | 26 / 35 |

## Architecture

Three layers with unidirectional data flow. MVI lives in the UI layer; domain and
data are plain Kotlin + Coroutines.

```
┌─────── UI (Compose + MVI) ────────┐
│  Screen ◀── StateFlow             │
│     │        collectAsStateWithLifecycle
│     ▼                              │
│  onIntent(Intent) ── ViewModel    │
│  Channel<Effect> ── navigation,   │
│                     toasts        │
└──────────── Domain ───────────────┘
│  UseCase: suspend invoke(): Result │
│  Pure models: Problem, Tier,       │
│  WeakTag, TrainingJob, Filters…    │
└──────────── Data ─────────────────┘
│  Repository (interface in domain)  │
│     ├── Room DAO                   │
│     ├── Retrofit CodeforcesApi     │
│     └── DataStore<Preferences>     │
└────────────────────────────────────┘
```

- **State**: single immutable data class per screen.
- **Intent**: sealed interface covering every user action.
- **Effect**: one-shot events via `Channel` + `receiveAsFlow()` (navigation,
  toasts) — never via `StateFlow`.
- **Reducers are pure.** Side effects flow through use cases or effects.

### Package layout

```
app/src/main/java/com/rakibjoy/problembuddy/
├── ProblemBuddyApp.kt            # @HiltAndroidApp + WorkManager Configuration.Provider
├── MainActivity.kt               # Theme + NavHost host
├── Navigation.kt                 # Type-safe destinations + bottom bar
├── AppRootViewModel.kt           # Decides Onboarding vs Home at launch
│
├── core/
│   ├── network/                  # CodeforcesApi, DTOs, NetworkModule
│   ├── database/                 # Room entities, DAOs, DatabaseModule
│   ├── datastore/                # SettingsStore, DataStoreModule
│   ├── work/                     # IngestWorker + IngestScheduler
│   └── ui/
│       ├── theme/                # Material 3 theme respecting ThemeMode
│       └── components/           # StaleDataBanner, EmptyCorpusCard
│
├── domain/                       # Pure Kotlin; no Android/androidx imports
│   ├── model/                    # Problem, Tier, Filters, TrainingJob, …
│   ├── repository/               # Interfaces
│   ├── usecase/                  # IngestHandleUseCase, ComputeWeakTagsUseCase,
│   │                             # GetRecommendationsUseCase, …
│   └── util/                     # HandleValidator
│
├── data/
│   ├── repository/               # Impls (CodeforcesRepositoryImpl, …)
│   ├── mapper/                   # DTO ↔ domain, Entity ↔ domain
│   ├── cache/                    # PersistedModels for offline fallback
│   ├── recommender/              # TierIndex (cosine ranking)
│   └── RepositoryModule.kt       # @Binds interface → impl
│
└── feature/
    ├── onboarding/               # Each feature is MVI:
    ├── home/                     #   State / Intent / Effect /
    ├── recommend/                #   ViewModel / Screen(.kt)
    ├── profile/                  #
    ├── settings/                 #
    └── train/                    #
```

## Recommendation algorithm

- Tag vocabulary is built per tier from the corpus (`data/recommender/TierIndex.kt`).
- Each problem becomes a 0/1 bag-of-words vector over that vocabulary.
- A "query" vector is the set of the user's weak tags. Cosine similarity picks the
  top-ranked unsolved problems.
- Weak tags (`domain/usecase/ComputeWeakTagsUseCase.kt`) are computed per tier:
  `coverage = solved_by_user / available_in_corpus`, ascending, with a minimum
  corpus-count threshold so tiny tag buckets don't dominate.

## Building locally

```bash
./gradlew assembleDebug                 # debug APK (app/build/outputs/apk/debug/)
./gradlew assembleRelease               # release APK, R8 + shrink + debug-signed
./gradlew testDebugUnitTest             # unit tests (JUnit 5)
./gradlew compileDebugAndroidTestKotlin # compile Compose/Room instrumentation tests
./gradlew connectedDebugAndroidTest     # run instrumentation tests (needs emulator)
```

Release builds enable R8 minification and resource shrinking. See
[`app/proguard-rules.pro`](app/proguard-rules.pro) for the kept classes —
kotlinx.serialization DTOs, Retrofit service interfaces (R8 full-mode recipe),
the `IngestWorker`, etc.

### Requirements

- JDK 17
- Android SDK 35 (`compileSdk`)
- Gradle 8.9 (wrapper included)

Open `settings.gradle.kts` at the repo root in Android Studio Ladybug or newer.

## Tests

Unit tests (`app/src/test/`):

- `Tier.forMaxRating` parametrised over every tier boundary.
- `TierIndex` cosine ranking with fixture corpora.
- `ComputeWeakTagsUseCase` ordering + minimum corpus-count filtering.
- `GetRecommendationsUseCase` end-to-end with mocked repositories.
- `HandleValidator` for the shared handle regex.
- `CodeforcesMappers`, `ProblemMappers`, `TrainingJobMappers`.

Instrumentation tests (`app/src/androidTest/`):

- `RecommendScreenTest` — skeleton state, problem cards render, filter IconButton
  emits the right intent, Mark-Solved removes the card.
- `ProblemBuddyDatabaseMigrationTest` — Room v1 → v2 auto-migration preserves
  existing rows and adds the `cached_payloads` table.

## CI

Two GitHub Actions workflows in [`.github/workflows/`](.github/workflows):

1. **`android.yml`** — on every push/PR to `main`:
   - Job 1: assemble debug + run unit tests + compile instrumentation tests.
   - Job 2: matrix over API levels 29 and 34, boot an emulator via
     [`reactivecircus/android-emulator-runner`](https://github.com/reactivecircus/android-emulator-runner),
     run `connectedDebugAndroidTest`. Uses AVD caching.
2. **`publish-apk.yml`** — on every push to `main` (or manual dispatch):
   - Builds the release APK.
   - Commits it to an orphan `downloads` branch as
     `ProblemBuddy-latest.apk` and `ProblemBuddy-<versionName>-<shortsha>.apk`
     plus a README.
   - `main` history stays free of binary bloat.

## Privacy

- Your Codeforces handle is stored in DataStore on-device.
- The app makes public, read-only calls to `codeforces.com/api/`. Nothing is
  sent anywhere else.
- Training submissions and the resulting corpus stay on-device in a Room database.

## Contributing

The single source of truth for the architecture and implementation order is
[`app/Implementation Plan.md`](app/Implementation%20Plan.md). Read it before making
non-trivial changes.

House rules (from `Implementation Plan.md` §8):

1. No `android.*` / `androidx.*` imports in `domain/`. Domain is pure Kotlin.
2. State mutates only through `_state.update { … }` inside the ViewModel reducer.
3. All IO/DB/network is `suspend` and dispatched to `Dispatchers.IO` at the
   repository boundary — never on the main thread.
4. `domain.model.Tier` is the only source of truth for rating thresholds.
5. One screen = one ViewModel. Share repositories, not ViewModels.

## Credits

- **Concept, algorithms, and web app** by [RakibJoy](https://codeforces.com/profile/RakibJoy)
  — see the original Django + React implementation at
  [TheRakibJoy/ProblemBuddy](https://github.com/TheRakibJoy/ProblemBuddy).
- Android port developed against the architecture in
  [`app/Implementation Plan.md`](app/Implementation%20Plan.md).

## License

[**PolyForm Noncommercial 1.0.0**](https://polyformproject.org/licenses/noncommercial/1.0.0/)
— free to use, modify, and share for personal, academic, research, and other
**noncommercial** purposes. Commercial use is not permitted without a separate
licence agreement from the author. See [`LICENSE`](LICENSE) for the full text.
