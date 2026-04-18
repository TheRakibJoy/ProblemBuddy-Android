# ProblemBuddy Android

A native Kotlin Android app that helps Codeforces users pick their next problem.

**The core idea: learn from who you look up to.** Add a handful of Codeforces
handles you find inspiring — `tourist`, `jiangly`, your favorite Red, the
specialist two bands ahead of you — and ProblemBuddy pulls all of their
accepted problems into one local library. The more handles you train on, the
richer and more accurate the recommendations, since the library spans a wider
set of problem styles and difficulty bands. Your practice — solved problems,
weak topics, contest history, daily streak — is then ranked against that
library so the app can nudge you toward the kinds of problems these stronger
players actually solve. No account, no server, no tracking. Everything runs
on your device.

This is the Android port of the web app at
[TheRakibJoy/ProblemBuddy](https://github.com/TheRakibJoy/ProblemBuddy) (Django +
React). Same recommendation logic, now running entirely on your phone.

Idea and original project by **[RakibJoy](https://codeforces.com/profile/RakibJoy)**.

> Release APKs are published automatically to the [`downloads`](../../tree/downloads)
> branch of this repo on every push to `main`.

## Download & install

1. On your Android phone, open the [`downloads`](../../tree/downloads) branch.
2. Tap `ProblemBuddy-latest.apk` → **Download**.
3. Open the APK. Android will prompt you to allow installs from your browser
   (one-time). Tap **Install**.

The APK is signed with a debug key, so Android shows an "unknown source" warning
— expected for side-loading. Minimum supported OS is **Android 8.0 (API 26)**.

## Screens

- **Onboarding** — enter your Codeforces handle; validated against
  `GET /api/user.info` before you can continue.
- **Home** — dashboard: upcoming contest countdown (registers via
  `contest.list`), streak-at-risk banner, rating / solved / streak stats,
  progress-to-next-tier pill, weekly goal progress, **problem of the day** card,
  today's picks, upsolve queue placeholder, rotating tip.
- **Recommend** — ranked problem list with left-rail rating, tier accent bar,
  tag chips. Swipe right to open, swipe left to skip; buttons for Solve / Mark
  Solved / Skip. Filter sheet with chip-based include/exclude tag selection,
  rating range, weak-only toggle, count. Staleness banner when data is cached.
- **Train** — framed as *"learn from who you look up to"*: add as many
  inspiring handles as you want — each one you ingest grows the corpus and
  sharpens recommendations, since more diverse sources cover more problem
  styles and difficulty bands. Shows a **corpus overview** (total problems,
  distinct tags, handle count, by-difficulty histogram), a live-validated
  handle input, progress card for the active ingest, and a list of
  previously-trained handles with re-sync / remove actions.
- **Profile** — hero with handle + tier badge + 3-stat row. Four tabs:
  1. **Tier ladder** — full vertical CF ladder, tier you're at is highlighted.
  2. **Weak tags** — coverage bars driven by `ComputeWeakTagsUseCase`.
  3. **Activity** — submissions heatmap (26 weeks), streak stats, full rating
     timeline, recent-contests list, division deltas, tier-stacked area,
     8-spoke tag radar, first-attempt AC rate, 90-day verdict breakdown,
     recently-failed problems, day-of-week + hour-of-day charts, language
     distribution, rated vs virtual counts, milestones feed, projected
     tier-ETA, one-year-ago snapshot.
  4. **Compare** — type another handle; see side-by-side rating and tier.
- **Settings** — appearance (theme), recommendations (per-load count, difficulty
  offset), goals (weekly solve target), notifications (daily problem reminder
  with time picker, hour + minute), data (reset corpus, delete all data),
  about (handle attribution + version + GitHub link).

## Key features

- **Codeforces-accurate tier colors and rating bands.** Newbie `#808080` through
  Legendary `#FF0000` match the site exactly. Legendary handles render with the
  first character in black per the CF convention.
- **Daily problem of the day** — deterministic per (date, handle), cached in
  DataStore. Same problem all day; rolls over at local midnight.
- **Daily notification** — opt-in `PeriodicWorkRequest` that fires at a user-
  chosen hour + minute via a `TimePicker`. Posts a notification with one
  recommended problem; tap opens it in the browser.
- **Incremental submission sync** — `user.status` is pulled in 100-item pages
  using a per-handle checkpoint, so active users don't re-download 10k+
  submissions on every refresh.
- **Offline resilience** — `userInfo`, `userStatus`, and upcoming contests are
  persisted in Room; if Codeforces is unreachable, the app falls back to cached
  data and shows a stale-data banner with age.
- **Weekly goal** — set a target, Home shows your progress against it.
- **Compare against another handle** — side-by-side rating and tier, opens the
  other user's CF profile in one tap.

## Stack

| Concern | Choice |
| --- | --- |
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 (Compose BOM 2024.10.01) |
| DI | Hilt 2.52 (KSP) |
| Local DB | Room 2.6.1 with auto-migrations v1 → v2 → v3 |
| Preferences | DataStore (Preferences) |
| Background work | WorkManager 2.9.1 + Hilt Worker |
| HTTP | Retrofit 2.11 + OkHttp 4.12 + kotlinx.serialization 1.7.3 |
| Images | Coil 2.7 (Codeforces avatar loading) |
| Async | Coroutines + Flow |
| Navigation | androidx.navigation.compose 2.8 (type-safe destinations) |
| Typography | `FontFamily.Monospace` (system) |
| Testing | JUnit 5 + MockK + Turbine (unit); Compose UI Test + Room `MigrationTestHelper` (instrumented) |
| Min / Target SDK | 26 / 35 |

## Architecture

Three layers with unidirectional data flow. MVI lives in the UI layer; domain
and data are plain Kotlin + Coroutines.

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
│  Filters, TrainingJob, Review,     │
│  ActivityStats, RatingPoint…       │
└──────────── Data ─────────────────┘
│  Repository (interface in domain)  │
│     ├── Room DAOs                  │
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
│   ├── network/                  # CodeforcesApi, DTOs (incl. ContestDto), NetworkModule
│   ├── database/                 # Room entities, DAOs, DatabaseModule (v3)
│   ├── datastore/                # SettingsStore, DataStoreModule
│   ├── work/                     # IngestWorker + IngestScheduler,
│   │                             # DailyProblemWorker + DailyProblemScheduler + Notifier
│   └── ui/
│       ├── theme/                # Material 3 theme, TierColors (CF-accurate),
│       │                         # AppTokens, Spacing, Shape, Elevation
│       └── components/           # Wordmark, AppTopBar, RatingRail, TagChip,
│                                 # HandleText, HandleAvatar, TierBadge,
│                                 # ActivityHeatmap, TagRadar, VerdictBar,
│                                 # FullRatingTimeline, UpcomingContestCard,
│                                 # StreakRiskBanner, NextTierProgress,
│                                 # DailyProblemCard, ReviewDueRow, etc.
│
├── domain/                       # Pure Kotlin; no Android/androidx imports
│   ├── model/                    # Problem, Tier, Filters, TrainingJob,
│   │                             # UpcomingContest, Review, Submission,
│   │                             # RatingChange, UserInfo, Fresh, …
│   ├── repository/               # Interfaces
│   ├── usecase/                  # IngestHandleUseCase, ComputeWeakTagsUseCase,
│   │                             # GetRecommendationsUseCase, GetTodayProblemUseCase, …
│   └── util/                     # HandleValidator
│
├── data/
│   ├── repository/               # Impls with incremental sync + persistent cache
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

1. Handle-tier: `Tier.forMaxRating(userInfo.maxRating ?: rating ?: 0)`.
2. **Difficulty window** (actually enforced): if no explicit filter, the use
   case computes `[rating + offset − 100, rating + offset + 200]` — the
   classic CP "slightly above your rating" practice rule. Settings' difficulty
   offset shifts the window.
3. Weak tags via `ComputeWeakTagsUseCase`: `coverage = solved / corpus`,
   ascending, top 10, with a minimum corpus-count threshold so tiny tag
   buckets don't dominate.
4. `TierIndex.build(problems)` builds a bag-of-words vector per problem.
5. `rank(weakTags)` returns problem indices sorted by cosine similarity.
6. Filters applied in order: solved set, interactions (solved / skipped /
   hidden), rating window, include tags, exclude tags, `weakOnly`.
7. Take `filters.count`.

## Codeforces rating bands

Matched to the site exactly:

| Tier | Range |
| --- | --- |
| Newbie | `< 1200` |
| Pupil | `1200–1399` |
| Specialist | `1400–1599` |
| Expert | `1600–1899` |
| Candidate Master | `1900–2099` |
| Master | `2100–2299` |
| International Master | `2300–2399` |
| Grandmaster | `2400–2599` |
| International Grandmaster | `2600–2999` |
| Legendary Grandmaster | `3000+` |

Tier color is canonical Codeforces hex (gray, green, cyan, blue, magenta,
orange, red). Master/Intl Master share the same orange and GM/IGM share the
same red — as on the site. Legendary handles render their first character in
black over the red, also matching CF.

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
the `IngestWorker` and `DailyProblemWorker`, etc.

### Requirements

- JDK 17
- Android SDK 35 (`compileSdk`)
- Gradle 8.9 (wrapper included)

Open `settings.gradle.kts` at the repo root in Android Studio Ladybug or newer.

## Tests

Unit tests (`app/src/test/`):

- `Tier.forMaxRating` parametrised over every CF rating boundary (19+ cases).
- `TierIndex` cosine ranking with fixture corpora.
- `ComputeWeakTagsUseCase` ordering + minimum corpus-count filtering.
- `GetRecommendationsUseCase` end-to-end with mocked repositories, includes
  difficulty-offset path.
- `HandleValidator` for the shared handle regex.
- `CodeforcesMappers` — all DTO → domain fields, protocol-relative avatar URL
  normalization, `ContestDto.toUpcoming()` parsing.
- `ProblemMappers`, `TrainingJobMappers`.

Instrumentation tests (`app/src/androidTest/`):

- `RecommendScreenTest` — skeleton state, problem cards render, filter
  IconButton emits the right intent, Mark-Solved (via overflow) removes the
  card.
- `ProblemBuddyDatabaseMigrationTest` — Room v1 → v2 migration preserves
  existing rows and adds the `cached_payloads` table.

## CI

Three GitHub Actions workflows in [`.github/workflows/`](.github/workflows):

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

- Your Codeforces handle, corpus, interactions, reviews, and settings stay
  on-device in Room and DataStore.
- The app makes public, read-only calls to `codeforces.com/api/`. Nothing else
  leaves the device — no telemetry, no crash reporting, no analytics.

## Contributing

The single source of truth for the architecture and implementation order is
[`app/Implementation Plan.md`](app/Implementation%20Plan.md). Read it before
making non-trivial changes.

House rules (from `Implementation Plan.md` §8):

1. No `android.*` / `androidx.*` imports in `domain/`. Domain is pure Kotlin.
2. State mutates only through `_state.update { … }` inside the ViewModel
   reducer.
3. All IO/DB/network is `suspend` and dispatched to `Dispatchers.IO` at the
   repository boundary — never on the main thread.
4. `domain.model.Tier` is the only source of truth for rating thresholds.
5. One screen = one ViewModel. Share repositories, not ViewModels.

## Credits

- **Concept, algorithms, and web app** by
  [RakibJoy](https://codeforces.com/profile/RakibJoy) — see the original Django
  + React implementation at
  [TheRakibJoy/ProblemBuddy](https://github.com/TheRakibJoy/ProblemBuddy).
- Android port developed against the architecture in
  [`app/Implementation Plan.md`](app/Implementation%20Plan.md).

## License

[**PolyForm Noncommercial 1.0.0**](https://polyformproject.org/licenses/noncommercial/1.0.0/)
— free to use, modify, and share for personal, academic, research, and other
**noncommercial** purposes. Commercial use is not permitted without a separate
licence agreement from the author. See [`LICENSE`](LICENSE) for the full text.
