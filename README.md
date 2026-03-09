# GreenBuddy

GreenBuddy is an Android game/app concept that combines:

- bite-sized plant-care learning
- a digital plant companion
- light tamagotchi / partner-game mechanics
- a PlantDex-style collection loop

## Current state

This repository now contains:

- a local Android/Compose app scaffold
- a documented MVP plan based on the shared ChatGPT conversation
- a workspace-local build setup via `../android-env`

## Run a build

```bash
cd /home/blue236/.openclaw/workspace/GreenBuddy
./gradlew assembleDebug
```

## Tech stack

- Kotlin
- Jetpack Compose
- Android SDK 35
- JDK 17

## Next recommended steps

1. Add onboarding flow
2. Add a real lesson/content model
3. Persist plant state with Room
4. Replace placeholder UI with art/assets
5. Add notifications and streak logic
