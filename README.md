# GreenBuddy

GreenBuddy is a local-first Android app/game prototype that blends:

- bite-sized plant-care lessons
- a digital plant companion
- lightweight care / streak / reward loops
- greenhouse-style collection and progression
- weather/reminder context and optional real-plant mirroring

## Current implementation status

This repo is no longer just a scaffold.

GreenBuddy already includes a working Compose MVP with:
- onboarding + starter selection
- Home / Learn / Greenhouse / Profile / Settings flows
- lesson progression with XP rewards
- care actions and plant-state persistence
- daily missions + streak rewards
- growth stages / unlock surfacing
- reward wallet + cosmetics foundation
- companion chat + proactive Home expression
- reminder notification foundation
- weather / seasonal advice foundation
- EN / KO / DE localization support

The current refactor direction focuses on **MVP stabilization**, not a rewrite:
- reducing `GreenBuddyViewModel` responsibility incrementally
- moving business logic behind domain engines/coordinators
- externalizing lesson/reminder content incrementally
- prioritizing Today’s Lesson more strongly on Home
- adding minimal analytics logging for validation

Recent stabilization progress already merged on `main` includes:
- initial domain seams:
  - `LessonEngine`
  - `RewardEngine`
  - `GrowthEngine`
  - `MissionEngine`
  - `CareEngine`
  - `CompanionCoordinator`
  - `RealPlantCoordinator`
- initial asset-backed lesson loading via `LessonContentLoader`
- externalized generic reminder copy loading via `ReminderCopyLoader`
- focused seam tests for mission/care/content/reminder behavior
- Home lesson-first CTA promotion
- slimmer lesson/care/companion orchestration inside `GreenBuddyViewModel`

## Tech stack

- Kotlin
- Jetpack Compose
- Android SDK 35
- JDK 17
- DataStore for local persistence
- WorkManager for reminder scheduling

## Build

```bash
cd /home/blue236/.openclaw/workspace/GreenBuddy
./gradlew assembleDebug
```

Run validation:

```bash
./gradlew testDebugUnitTest assembleDebug
```

## Project structure

High-level areas:
- `app/src/main/java/com/blue236/greenbuddy/ui` — app shell, screens, UI state
- `app/src/main/java/com/blue236/greenbuddy/model` — app/domain models and legacy logic helpers
- `app/src/main/java/com/blue236/greenbuddy/data` — persistence + content loading
- `app/src/main/java/com/blue236/greenbuddy/domain` — extracted engines/coordinators for MVP stabilization
- `app/src/main/java/com/blue236/greenbuddy/notifications` — reminder scheduling/notifier/worker
- `app/src/main/assets/content` — externalized content assets (incremental)
- `docs/` — roadmap, tracker, product notes

## Refactor Plan v1 focus

Current stabilization goals:
1. keep docs synchronized with implementation reality
2. continue splitting business logic out of `GreenBuddyViewModel`
3. continue externalizing lesson/content/notification text incrementally
4. keep Today’s Lesson as the primary Home CTA and validate hierarchy on device
5. extend minimal local analytics/event logging hooks carefully

## Notes

- The app remains **local-first**.
- The current work intentionally prefers **incremental refactor over rewrite**.
- Content externalization is currently partial and expected to expand over time.
- The stabilization work is being delivered as small follow-up PRs on top of `main`, not as a single large rewrite.
