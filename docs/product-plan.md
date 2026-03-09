# GreenBuddy Product Plan

## Source idea distilled
The shared ChatGPT thread converged on the strongest direction as a hybrid:

- Duolingo-style micro learning
- Finch-style emotional companion
- Pokémon-like collection / progression
- optional real-world plant care linkage

## Product direction chosen for scaffold
**Reality-linked plant companion learning app**

Short version:
> Learn plant care in short sessions, grow a digital plant companion, and gradually unlock a broader greenhouse and PlantDex.

## MVP goal
Deliver a 3-5 minute daily loop that feels rewarding even without multiplayer, advanced art, or backend services.

## MVP daily loop
1. Open app
2. See plant companion and status
3. Play one short lesson
4. Answer one quiz
5. Earn XP and stat bonuses
6. Do one care action
7. Preview next growth reward

## MVP feature set
- 3 starter plants: Monstera, Basil, Tomato
- 1 companion per active plant
- 20 lesson skeletons later; app scaffold currently includes example lesson content only
- 5 core stats: hydration, sunlight, nutrition, health, mood
- 4 growth stages for MVP: Seed, Sprout, Young Plant, Mature Plant
- PlantDex list
- profile/streak placeholder

## Screen map
- Welcome / onboarding
- Home hub
- Lesson
- Care state
- Growth reward modal/screen
- PlantDex
- Profile

## Suggested phase plan
### Phase 1
- local-only single-player
- static content
- no login
- no backend

### Phase 2
- Room persistence
- notifications
- streak system
- greenhouse layout
- unlock economy

### Phase 3
- optional cloud sync
- community screenshots/timeline
- real-plant reminders and personalization

## Design principles
- calm, warm, soft colors
- low friction, short sessions
- visible plant reactions after every successful action
- never punish too harshly; wilt instead of death

## Core balancing assumptions
- one successful lesson: +20 XP
- one care action: +5 to +20 stat depending on context
- stage-up target for first evolution: about 100-120 XP
- streak rewards every 3/7 days for early retention

## Technical architecture recommendation
- Kotlin + Jetpack Compose
- MVVM-ish presentation separation
- Room for offline persistence
- DataStore for settings
- WorkManager for reminders

## Immediate backlog
1. Build onboarding flow
2. Define domain models and fake repository
3. Add navigation with real screens
4. Add Room entities
5. Add lesson JSON/content pipeline
6. Implement growth algorithm
