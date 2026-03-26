# GreenBuddy Development Tracker

This document is the execution companion to `docs/feature-roadmap.md`.

- `feature-roadmap.md` = product direction / why / rough order
- `development-tracker.md` = current implementation status / next tasks / progress tracking

Use this file to keep `TODO`, `IN PROGRESS`, and `DONE` updated as work moves.

---

## Current snapshot

### Branch context
- Current active branch when this tracker was created: `feature/f12-phase3-continuity-emotion`

### Product state summary
GreenBuddy is **past bare MVP** and already contains working foundations for:
- starter selection
- lesson progression
- care actions
- reminder notifications
- localization (EN/KO/DE foundation)
- weather / seasonal feedback
- haptic polish
- reward economy foundation
- greenhouse / inventory foundation
- real-plant mode foundation
- companion chat, proactive expression, and continuity/emotion layers

### Strategic reading of the current state
The companion-related work (especially F12) is ahead of the original roadmap order.
The next high-value work should likely focus on:
1. polishing retention loops already in the app
2. improving visibility / UX clarity of implemented systems
3. expanding content depth where the foundation is already done

---

# Status board

## DONE

### T4. F5 reward economy polish
**Status:** DONE

Delivered in this pass:
- Home reward visibility improved with a dedicated reward overview area
- wallet balance, token purpose, next unlock, equipped cosmetic, and reward feedback are surfaced more clearly on Home
- reward-goal helpers added to support next-unlock and tokens-needed UI
- Profile reward/shop clarity improved with a dedicated wallet/reward grouping card
- Profile shop rows now show cosmetic cost and clearer affordability / status states
- reward helper tests expanded for cosmetic purchase/equip and unlock-goal behavior
- reward composition logic extracted into `MissionRewardEvaluator` for focused testing
- unit tests added to verify daily/streak reward composition and duplicate-reward prevention

Notes:
- This is strong enough to count as DONE for T4.
- Remaining opportunities are polish only (for example onboarding-level token-purpose messaging, playtest-based pacing/cost tuning, or small additional Profile/UI assertions).

---

### T3. F12 validation / stabilization
**Status:** DONE

Delivered in this pass:
- companion continuity/streak classification reviewed and stabilized
- continuity priority adjusted so growth unlocks and healthier streak states read more naturally
- healthy partial-progress streak state no longer leans too easily into at-risk tone
- companion unit tests expanded for chip distinctness/bounds and familiarity-threshold determinism
- continuity precedence / streak-tone behavior covered by tests
- Home proactive companion density reduced
- suggestion-chip role separation improved with lightweight de-duplication
- familiarity / bond phrasing softened to feel less eager
- companion stabilization APK built and shared for device-level review

Notes:
- This is strong enough to count as DONE for T3.
- Remaining opportunities are polish only (for example extra device-smoke tuning, additional UI assertions, or further localization nuance passes).

---

### T2. F2 growth UX polish
**Status:** DONE

Delivered in this pass:
- compact Home growth card added near the top of Home
- growth visibility improved with stage title, emoji, readiness chip, progress bar, next-stage context, and requirement summary
- Home growth unlock celebration upgraded with stronger species-specific copy
- Profile growth section polished for clearer hierarchy and stronger progression flavor
- Home hierarchy received a final small simplification pass to reduce redundant growth-chip noise
- localized strings for the new growth UI added
- model-level growth test coverage extended
- minimal Home growth/unlock UI coverage added
- Profile growth UI test added

Notes:
- This is strong enough to count as DONE for T2.
- Remaining opportunities are polish only (for example optional unlock delight/haptics, extra Home growth UI assertions, or tighter real-device hierarchy tuning on very small screens).

---

### T1. F1 mission UX polish
**Status:** DONE

Delivered in this pass:
- Daily Missions card moved higher in the Home hierarchy
- Home mission summary expanded into a visible checklist card
- compact progress / streak / bonus chip-style surfacing added
- mission reward summary made clearer on Home
- mission-completion reward feedback now takes priority over generic action-only feedback when the full set is completed
- mission UI copy localized for EN / KO / DE
- model-level mission/streak tests expanded
- minimal Home mission card UI test added

Notes:
- This pass is strong enough to count as done for T1.
- Future polish is still possible (for example missed-day recovery copy, richer milestone celebration, extra UI coverage), but the core T1 goal is now met.

---

### F1. Daily missions + streak system — foundation implemented
**Status:** DONE (foundation) / needs polish

Implemented:
- one mission set per day
- 3 mission types
  - complete one lesson
  - perform one care action
  - keep one stat above threshold
- streak tracking
- daily reward + streak bonus reward
- persistence across launches
- Home mission summary card
- Profile streak summary

Code anchors:
- `app/src/main/java/com/blue236/greenbuddy/model/DailyMissionModels.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/state/GreenBuddyViewModel.kt`
- `app/src/main/java/com/blue236/greenbuddy/data/GreenBuddyPreferencesRepository.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/screens/HomeScreen.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/screens/ProfileScreen.kt`

Notes:
- Core logic exists and is wired into lesson/care actions.
- Remaining work is mainly UX clarity, surfacing, and player feedback.

---

### F2. Growth and evolution thresholds — implemented
**Status:** DONE (system) / needs presentation polish

Implemented:
- explicit growth stage rules
- per-species growth thresholds
- XP + care score gating
- growth unlock detection
- persistence for seen growth stage rank
- Profile growth status UI
- unlock acknowledgement flow

Code anchors:
- `app/src/main/java/com/blue236/greenbuddy/model/GrowthModels.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/state/GreenBuddyViewModel.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/screens/ProfileScreen.kt`

Notes:
- This is already real progression logic, not just decorative labeling.
- Remaining work is mainly stronger presentation on Home / greenhouse / unlock moments.

---

### F3. Lesson content expansion + quiz variety — baseline implemented
**Status:** DONE (baseline) / content expansion still open

Implemented:
- per-species lesson tracks
- quiz variety
  - multiple choice
  - true/false
  - scenario choice
- lesson summaries / concepts / key takeaways / reward labels
- starter track distinction
- tests for lesson coverage and quiz variety

Code anchors:
- `app/src/main/java/com/blue236/greenbuddy/model/LessonCatalog.kt`
- `app/src/main/java/com/blue236/greenbuddy/model/LessonModels.kt`
- `app/src/main/java/com/blue236/greenbuddy/model/LessonProgression.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/screens/LearnScreen.kt`
- `app/src/test/java/com/blue236/greenbuddy/model/LessonCatalogTest.kt`
- `app/src/test/java/com/blue236/greenbuddy/model/LessonProgressionTest.kt`

Notes:
- The baseline roadmap goal is met.
- The open question is now content depth, not system existence.

---

### F6. Notifications / reminder loop — implemented
**Status:** DONE

Implemented:
- reminder scheduling
- worker execution
- reminder decision rules
- notification posting
- destination routing on click

Code anchors:
- `app/src/main/java/com/blue236/greenbuddy/model/ReminderModels.kt`
- `app/src/main/java/com/blue236/greenbuddy/notifications/ReminderScheduler.kt`
- `app/src/main/java/com/blue236/greenbuddy/notifications/ReminderWorker.kt`
- `app/src/main/java/com/blue236/greenbuddy/notifications/ReminderNotifier.kt`
- `app/src/main/java/com/blue236/greenbuddy/notifications/ReminderLaunch.kt`

---

### F9. Localization foundation + Korean/German support — implemented
**Status:** DONE

Implemented:
- localization-ready UI copy structure
- app language setting
- Korean / German support across key surfaces
- localized lesson/personality/reminder text foundation

Code anchors:
- `app/src/main/java/com/blue236/greenbuddy/model/Localization.kt`
- `app/src/main/java/com/blue236/greenbuddy/model/AppLanguage.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/screens/SettingsScreen.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-ko/strings.xml`
- `app/src/main/res/values-de/strings.xml`

---

### F10. Weather + seasonal feedback — implemented foundation
**Status:** DONE (foundation)

Implemented:
- selected city / weather snapshot
- seasonal summary feedback
- starter-context advice on Home

Code anchors:
- `app/src/main/java/com/blue236/greenbuddy/model/WeatherModels.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/state/GreenBuddyViewModel.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/screens/HomeScreen.kt`

---

### F11. Haptic / feedback polish — implemented foundation
**Status:** DONE (foundation)

Implemented:
- feedback event pipeline tied to success / unlock flows

Code anchors:
- `app/src/main/java/com/blue236/greenbuddy/model/FeedbackModels.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/state/GreenBuddyViewModel.kt`

---

### F12. Plant AI companion mode — Phases 1-3 implemented as deterministic/state-aware system
**Status:** DONE (current planned phases)

Implemented:
- Phase 1: state-aware companion chat MVP
- Phase 2: proactive companion expression
- Phase 3: continuity + proactive emotion

Code anchors:
- `app/src/main/java/com/blue236/greenbuddy/model/CompanionChatModels.kt`
- `app/src/main/java/com/blue236/greenbuddy/model/PersonalityModels.kt`
- `app/src/main/java/com/blue236/greenbuddy/model/HomeCompanionInsights.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/screens/HomeScreen.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/state/GreenBuddyViewModel.kt`

Notes:
- Companion systems are ahead of the original roadmap order.
- Immediate next step here should be validation/polish, not major expansion.

---

## IN PROGRESS

### T5. F4 greenhouse loop polish
**Status:** IN PROGRESS

Current pass status:
- [x] Dex/greenhouse cards now surface growth-stage progress for owned plants
- [x] Owned vs active messaging is clearer and more motivating
- [x] Locked entries use clearer localized unlock-requirement copy
- [x] Greenhouse is more visibly connected to growth/evolution progress
- [x] Inventory model tests expanded around switching/fallback/localized unlock-requirement behavior
- [x] Reconciled and verified branch health around the reported `unlockRequirementFor(...)` compile conflict
- [x] Added a small Dex UI test for active / owned / locked label states after layout stabilized
- [x] Added focused unit coverage for greenhouse unlock feedback composition and localized starter naming
- [x] Added a greenhouse top summary card for current active plant / next unlock target / collection momentum
- [x] Added a stronger unlock-earned moment by surfacing new greenhouse buddy unlock feedback when a track completes

Director note:
- T5 has a good first-pass direction: it improves meaning and readability rather than adding a new system.
- Keep it active until branch health is confirmed clean and the collection loop feels more rewarding in actual use.

---

### F4. Plant inventory / greenhouse
**Status:** IN PROGRESS

What exists:
- owned / unlocked companion model support
- active starter switching
- greenhouse / dex screen exists
- per-starter lesson progress and care state persistence exists

Code anchors:
- `app/src/main/java/com/blue236/greenbuddy/model/InventoryModels.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/screens/DexScreen.kt`
- `app/src/main/java/com/blue236/greenbuddy/data/GreenBuddyPreferencesRepository.kt`

What still needs work:
- make the collection loop feel more meaningful
- strengthen unlock visibility and collection motivation
- connect greenhouse progression more clearly to growth / rewards / discovery

---

### F5. Reward economy + cosmetics
**Status:** IN PROGRESS

What exists:
- token economy foundation
- cosmetic catalog / purchase / equip flow
- reward shop UI
- reward feedback strings and pathways

Code anchors:
- `app/src/main/java/com/blue236/greenbuddy/model/RewardModels.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/screens/ProfileScreen.kt`
- `app/src/main/java/com/blue236/greenbuddy/ui/state/GreenBuddyViewModel.kt`

What still needs work:
- make rewards feel more purposeful and visible
- improve reward surfacing in Home / mission loop / progression loop
- evaluate whether economy pacing is satisfying

---

### F7. Companion personality system
**Status:** IN PROGRESS

What exists:
- species-based personality profiles
- reactive/state-aware dialogue
- stronger companion continuity and emotion

What still needs work:
- validate that personality differences feel distinct enough in normal use
- ensure tone does not become repetitive
- tighten UX around proactive companion visibility

---

### F8. Real-world plant mode
**Status:** IN PROGRESS

What exists:
- mode toggle
- real plant action logging
- tie-in to plant care state

What still needs work:
- make the mode feel more differentiated / valuable
- decide whether checklist/log is enough or whether richer journaling is needed
- improve how this mode is explained and surfaced

---

## TODO

### Priority P1 — Finch-inspired orchestration polish

#### T1. Home hierarchy / command-center pass
- [ ] Rebuild Home around a stricter hierarchy: companion hero → today’s missions → growth progress → quick care → contextual extras
- [ ] Add one clear “best next action” CTA near the top of Home
- [ ] Reduce equal-weight card density so the screen answers “how is my buddy / what should I do next / what do I get?” at a glance
- [ ] Move secondary systems behind progressive disclosure where possible
- [ ] Validate that Home feels calmer and lower-cognitive-load on device

#### T2. Mission ritual / daily motivation pass
- [ ] Turn missions into a more visual 3-step ritual with icon, plain-language title, tiny reward cue, and clearer completion state
- [ ] Add a stronger “one more to finish today” progress arc
- [ ] Add warmer streak milestone celebration with softer plant-themed framing
- [ ] Rework missed-day / streak reset copy to feel encouraging rather than punitive
- [ ] Add or expand tests around mission completion and streak milestone UX where practical

#### T3. Companion hero UX pass
- [ ] Promote the companion into a true Home hero zone with one emotionally readable line and one high-value suggestion chip
- [ ] Make companion text act as the emotional guide for missions/growth rather than a parallel feature card
- [ ] Add lightweight visual emotion cues (expression/posture/color/motion/haptic) without making the UI noisy
- [ ] Keep companion guidance short enough to feel immediate rather than chatty
- [ ] Run a dedicated copy/localization nuance pass for companion tone (especially KO/DE)

#### T4. Growth clarity / unlock delight pass
- [ ] Keep growth visibility above the fold with clearer “how close / what helps most / what unlocks next” communication
- [ ] Strengthen unlock/evolution moments with a larger celebratory sheet or modal-level reveal
- [ ] Make species-specific growth differences more visible in copy and progression framing
- [ ] Add direct follow-up actions from unlock moments (for example “See in Greenhouse” / “Keep growing”)
- [ ] Validate that daily actions feel clearly connected to growth progress

---

### Priority P2 — retention loop depth and warmth

#### T5. Reward purpose / reward-track clarity
- [ ] Make token purpose more obvious earlier in onboarding and Home
- [ ] Surface current wallet + nearest meaningful unlock in a lighter-weight Home strip
- [ ] Refine combined reward feedback wording so action, mission, and unlock rewards stack more clearly
- [ ] Review token pacing and cosmetic costs after more device/play testing
- [ ] Consider whether a softer self-expression framing would make cosmetics feel more meaningful

#### T6. Greenhouse as a place, not just a list
- [ ] Reframe Greenhouse to feel more like a warm room/garden space than a plain dex list
- [ ] Strengthen collection overview with active plant / collection progress / next unlock / recent milestone at the top
- [ ] Make owned / active / locked states more visually distinct and collectible-feeling
- [ ] Add mini bio / growth history / favorite conditions / unlock path detail for companions over time
- [ ] Clean up inventory unlock-requirement internal representation so UI text stays locale-safe by design

#### T7. Multi-horizon progression visibility
- [ ] Make “today / this week / long-term” progression more explicit across Home, Profile, and Greenhouse
- [ ] Add simple continuity language such as recent care streaks, weekly growth movement, and milestone counts
- [ ] Make Profile feel more like identity/history and less like a settings-adjacent status dump

#### T8. Real plant mode value framing
- [ ] Reposition real plant mode as a bridge between digital buddy and real plant care, not just a toggle
- [ ] Surface clearer user benefits (logging, reminders, reflection, seasonal support)
- [ ] Add light Home acknowledgment when real-plant mode is enabled and drifting stale
- [ ] Decide whether a mini timeline/journal should replace or complement checklist-style logging

---

### Priority P3 — onboarding, settings, and content depth

#### T9. Onboarding clarity / emotional framing
- [ ] Rework onboarding as a short story: choose starter → meet personality → understand growth → see reward loop
- [ ] Teach the Home hierarchy explicitly during onboarding
- [ ] Show one example unlock/reward early so currency and progression have immediate meaning
- [ ] Consider reminder tone/frequency setup during onboarding rather than burying it later

#### T10. Settings personalization / tone controls
- [ ] Reorganize settings around user intent: companion, reminders, language, accessibility/motion, real plant mode
- [ ] Let users tune companion intensity / reminder softness / haptics more explicitly
- [ ] Preview tone choices for notifications and companion prompts where useful

#### T11. Visual design warmth pass
- [ ] Soften UI density with more breathing room and fewer equally heavy hard-edged cards
- [ ] Use warmer backgrounds and species-linked accent colors more intentionally
- [ ] Make companion/plant art more central than generic UI chrome
- [ ] Add motion sparingly for idle life and milestone delight

#### T12. F3 lesson expansion
- [ ] Increase lesson count per starter beyond the current baseline
- [ ] Add richer wrong-answer feedback or explanation copy
- [ ] Review lesson progression difficulty curve
- [ ] Expand species differentiation in lesson themes and rewards

---

# Recommended next implementation order

## Next 1-2 tasks
1. **T1. Home hierarchy / command-center pass**
2. **T2. Mission ritual / daily motivation pass**
3. **T3. Companion hero UX pass**

## After that
4. **T4. Growth clarity / unlock delight pass**
5. **T5. Reward purpose / reward-track clarity**
6. **T6. Greenhouse as a place, not just a list**
7. **T7. Multi-horizon progression visibility**
8. **T8. Real plant mode value framing**
9. **T9-T11 onboarding / settings / visual warmth passes**
10. **T12. F3 lesson expansion**

---

# Update protocol

When work progresses, update this document using the following rules:

## Move work between sections
- Start not-yet-started work in `TODO`
- Move active work to `IN PROGRESS`
- Move completed work to `DONE`

## Keep statuses honest
Use these meanings:
- **TODO** = not started or only loosely discussed
- **IN PROGRESS** = code/design work has started but success criteria are not yet satisfied
- **DONE** = the feature works in product terms, even if future polish is still possible

## Prefer small checklists under each active task
Example:
- [x] implemented state model
- [x] wired persistence
- [ ] added Home UI
- [ ] validated on device
- [ ] added tests

## Update after each meaningful milestone
At minimum, update after:
- finishing a feature branch milestone
- merging a feature PR
- changing product priority
- discovering that a feature thought to be done is actually incomplete

---

# Suggested next update
If development resumes immediately, the next update should likely:
- move `T1. F1 mission UX polish` into `IN PROGRESS`
- add a concrete checklist for the first Home mission card improvement pass
