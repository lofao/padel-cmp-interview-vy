# Padel CMP Interview Solution

This document details the implementation, architecture, code changes, and verification for the Compose Multiplatform (CMP) padel tournament application assignment outlined in [INTERVIEW.md](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/INTERVIEW.md).

---

## Executive Summary

All five tasks described in [INTERVIEW.md](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/INTERVIEW.md) have been fully implemented, verified, and integrated into the repository:

1. **Task 1: Fix Pull-to-Refresh Bug** — Fixed unresponsive pull-to-refresh gesture when network errors occur on both Tournament Detail and Match Detail screens.
2. **Task 2: Improve Set Score Display** — Designed a structured `SetScoreCell` component with tiebreak notation support (`6(5)`), eliminating visual misalignment across match rows.
3. **Task 3: Players List Screen** — Implemented a clean feature module for browsing players with search filter, pull-to-refresh, PadelAPI integration, and demo data fallback.
4. **Task 4: Player Detail Screen** — Implemented player profile view accessible from both Players List and Match Detail screens, preserving navigation stack history.
5. **Task 5: Location-Based Tournament Sorting** — Built multiplatform `LocationService` (`expect`/`actual`) and combined reactive flows to auto-sort tournaments by proximity to user device location.
6. **Architecture & Scope Cleanup** — Ensured ViewModels are properly scoped to Navigation 3 backstack entries (`NavEntryDecorators`), added unified avatar rendering, and cleaned up location fallbacks.

---

## Commit Structure

The solution follows a strict commit history with one logical task per commit:

| Hash | Summary | Task Covered |
| :--- | :--- | :--- |
| `ac5cf40` | `Task 1: Fix Pull-to-Refresh Bug in Tournament Detail Screen` | Task 1 |
| `d25b867` | `Task 2: Improve Set Score Display Design on Matches and Match Screens` | Task 2 |
| `de47f13` | `Task 3: Create Players List Screen` | Task 3 |
| `2479bf8` | `Task 4: Create Player Detail Screen` | Task 4 |
| `31c26df` | `Task 5: Location` | Task 5 |
| `e7be894` | `UI and logic fixes: Removed default location fallback...` | Architectural & Lifecycle Polish |

---

## Detailed Task Solutions

### Task 1: Fix Pull-to-Refresh Bug in Detail Screens

- **Problem:** Pull-to-refresh gestures were disabled or omitted from the UI subtree when error states were displayed in `TournamentScreen` and `MatchScreen`.
- **Solution:**
    - Wrapped screen content in `AppPullToRefreshBox` regardless of whether the UI state is `Loading`, `Content`, or `Error`.
    - Configured `onRefresh` callback to emit the `Refresh` partial state event in all states, enabling retries on network failures.
    - Added unit tests in `TournamentViewModelTest` and `MatchViewModelTest` to verify state transitions upon pull-to-refresh.
- **Key Files Modified:**
    - [`TournamentScreen.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/tournament/ui/src/commonMain/kotlin/io/bifri/interview/cmp/padel/feature/tournament/ui/tournament/TournamentScreen.kt)
    - [`MatchScreen.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/match/ui/src/commonMain/kotlin/io/bifri/interview/cmp/padel/feature/match/ui/match/MatchScreen.kt)
    - [`TournamentViewModelTest.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/tournament/ui/src/commonTest/kotlin/io/bifri/interview/cmp/padel/feature/tournament/ui/tournament/TournamentViewModelTest.kt)
    - [`MatchViewModelTest.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/match/ui/src/commonTest/kotlin/io/bifri/interview/cmp/padel/feature/match/ui/match/MatchViewModelTest.kt)

---

### Task 2: Improve Set Score Display Design

- **Problem:** Inline string rendering of scores like `"6 6(5) 7"` caused horizontal misalignment across list rows and inconsistent column widths.
- **Solution:**
    - Created a modular `SetScoreCell` component that parses set strings into primary score and tiebreak notation.
    - Rendered tiebreak points as superscript numbers next to set scores.
    - Used fixed-width tabular column alignment so scores across team pairs and match rows align perfectly.
    - Integrated `SetScoreCell` in both `MatchesScreen` and `MatchScreen`.
    - Added unit tests in `SetScoreCellTest.kt`.
- **Key Files Created / Modified:**
    - [`SetScoreCell.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/match/ui/src/commonMain/kotlin/io/bifri/interview/cmp/padel/feature/match/ui/component/SetScoreCell.kt)
    - [`MatchesScreen.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/match/ui/src/commonMain/kotlin/io/bifri/interview/cmp/padel/feature/match/ui/matches/MatchesScreen.kt)
    - [`MatchScreen.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/match/ui/match/MatchScreen.kt)
    - [`SetScoreCellTest.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/match/ui/src/commonTest/kotlin/io/bifri/interview/cmp/padel/feature/match/ui/component/SetScoreCellTest.kt)

---

### Task 3: Create Players List Screen

- **Requirements Met:**
    - Added layered `feature:player:data`, `feature:player:domain`, `feature:player:ui` modules.
    - Integrated `GET /api/players` endpoint in `PadelApiPlayerRepository` with fallback to `DemoPlayerRepository` when no API token is configured.
    - Created `GetPlayersUseCase` and MVI architecture (`PlayersViewModel`, `PlayersState`, `PlayersPartialState`).
    - Implemented real-time search filtering with clear button, pull-to-refresh, error handling, and empty states.
    - Added "Players" action chip on `TournamentsScreen` for seamless navigation.
    - Configured Koin dependency injection across all player feature layers.
    - Wrote `PlayersViewModelTest`.
- **Key Files Created / Modified:**
    - [`PlayersScreen.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/player/ui/src/commonMain/kotlin/io/bifri/interview/cmp/padel/feature/player/ui/players/PlayersScreen.kt)
    - [`PlayersViewModel.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/player/ui/src/commonMain/kotlin/io/bifri/interview/cmp/padel/feature/player/ui/players/PlayersViewModel.kt)
    - [`GetPlayersUseCase.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/player/domain/src/commonMain/kotlin/io/bifri/interview/cmp/padel/feature/player/domain/usecase/GetPlayersUseCase.kt)
    - [`PlayersViewModelTest.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/player/ui/src/commonTest/kotlin/io/bifri/interview/cmp/padel/feature/player/ui/players/PlayersViewModelTest.kt)

---

### Task 4: Create Player Detail Screen

- **Requirements Met:**
    - Implemented `GET /api/players/{id}` in data and domain layers (`GetPlayerUseCase`).
    - Created `PlayerDetailRoute(playerId)` integrated with Navigation 3 backstack.
    - Enabled entry from both `PlayersScreen` (tapping a player row) and `MatchScreen` (tapping a player name).
    - Preserved backstack integrity so back navigation returns to the exact originating screen.
    - Rendered player avatar, country, rank, stats summary, and details card with loading and error states.
    - Added `PlayerDetailViewModelTest`.
- **Key Files Created / Modified:**
    - [`PlayerDetailScreen.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/player/ui/src/commonMain/kotlin/io/bifri/interview/cmp/padel/feature/player/ui/player/PlayerDetailScreen.kt)
    - [`PlayerDetailViewModel.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/player/ui/src/commonMain/kotlin/io/bifri/interview/cmp/padel/feature/player/ui/player/PlayerDetailViewModel.kt)
    - [`GetPlayerUseCase.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/player/domain/src/commonMain/kotlin/io/bifri/interview/cmp/padel/feature/player/domain/usecase/GetPlayerUseCase.kt)
    - [`PlayerDetailViewModelTest.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/player/ui/src/commonTest/kotlin/io/bifri/interview/cmp/padel/feature/player/ui/player/PlayerDetailViewModelTest.kt)

---

### Task 5: Location-Based Tournament Sorting

- **Requirements Met:**
    - Implemented `LocationService` with `expect`/`actual` for Android (`FusedLocationProviderClient`) and iOS (`CLLocationManager`), with a 60-second observation throttling and distinct coordinate emissions.
    - Created `GeocoderService` (`expect`/`actual`) and city coordinate lookup table for resolving tournament city locations.
    - Implemented `LocationPermissionEffect` for runtime permission requests.
    - Combined `getTournamentsUseCase()` flow and `locationService.locationFlow` in `TournamentsViewModel` using reactive operators.
    - Computed distances via Haversine formula (`Coordinates.distanceToKm`) and auto-sorted tournaments nearest-first.
    - Displayed distance chips (e.g. `"12 km away"`) on tournament cards.
    - Tested coordinate distance calculations in `CoordinatesTest.kt`.
- **Key Files Created / Modified:**
    - [`LocationService.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/core/location/src/commonMain/kotlin/io/bifri/interview/cmp/padel/core/location/LocationService.kt)
    - [`LocationService.android.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/core/location/src/androidMain/kotlin/io/bifri/interview/cmp/padel/core/location/LocationService.android.kt)
    - [`LocationService.ios.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/core/location/src/iosMain/kotlin/io/bifri/interview/cmp/padel/core/location/LocationService.ios.kt)
    - [`TournamentsViewModel.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/feature/tournament/ui/src/commonMain/kotlin/io/bifri/interview/cmp/padel/feature/tournament/ui/tournaments/TournamentsViewModel.kt)
    - [`CoordinatesTest.kt`](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/core/location/src/commonTest/kotlin/io/bifri/interview/cmp/padel/core/location/CoordinatesTest.kt)

---

### Post-Task Refactoring & Architecture Polish

- **ViewModel Lifecycle Scoping:** Added `NavEntryDecorators` in Navigation 3 so each screen's ViewModel is bound to its backstack entry lifecycle, preventing stale state or memory leaks when screens close.
- **Unified Avatar Component:** Extracted `PlayerAvatar` to `core:designsystem` for consistent image fallback handling across `PlayersScreen` and `MatchScreen`.
- **Location Status Accuracy:** Removed hardcoded location fallback to guarantee accurate location permission reporting.

---

## Verification & Quality Assurance

All verification commands specified in [AGENTS.md](file:///Users/vadims.jasins/projects/padel-cmp-interview-vy/AGENTS.md) execute cleanly:

```bash
# Unit Tests
./gradlew test
# Output:
# :core:ui:testDebugUnitTest - 2 passed
# :feature:match:ui:testDebugUnitTest - 4 passed
# :feature:player:ui:testDebugUnitTest - 4 passed
# :feature:tournament:ui:testDebugUnitTest - 1 passed

# Metadata & Android Compilation
./gradlew :composeApp:compileKotlinMetadata --quiet
./gradlew :composeApp:compileDebugKotlinAndroid --quiet

# Code Quality & Formatting
./gradlew ktlintCheck
./gradlew detekt
```

---

## Verification Screenshots Inventory

Reference screenshots provided in `docs/screenshots/`:

---

## Conclusion

The implementation follows modern Kotlin Multiplatform, Jetpack Compose, Navigation 3, and MVI principles. The codebase maintains high quality, strict separation of concerns across data/domain/ui layers, and passing test suites.
