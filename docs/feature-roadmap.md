# GreenBuddy Feature Roadmap

This roadmap is optimized for the current MVP state of GreenBuddy:
- starter selection works
- lesson progression works
- care actions work
- basic Home/Learn/Profile flows exist
- app builds and can be tested on device

## Delivery workflow
For each feature:
1. Steve implements on a feature branch
2. Build/test validation runs
3. A separate reviewer sub-agent reviews code quality and risks
4. Fixes are applied if needed
5. PR is opened/updated
6. Merge after approval

## Priority order

### F1. Daily missions + streak system
**Goal:** Create a daily reason to open the app.

Scope:
- one daily mission set per day
- 2-3 simple mission types
  - complete one lesson
  - perform one care action
  - keep one stat above a threshold
- streak tracking
- daily reward + streak reward
- Home screen mission card

Success criteria:
- user can see today’s missions
- mission completion persists
- streak persists across launches
- rewards are visible and understandable

---

### F2. Growth and evolution thresholds
**Goal:** Make plant development feel earned and species-specific.

Scope:
- explicit growth stage thresholds
- per-species growth tuning
- visible unlock/evolution moments
- growth state reflected in Home and Profile

Success criteria:
- growth stage is tied to progression rules, not only display logic
- each starter can feel slightly different

---

### F3. Lesson content expansion + quiz variety
**Goal:** Make the app feel educational, not just structurally complete.

Scope:
- expand lesson catalog per starter
- add quiz variety:
  - multiple choice
  - true/false
  - scenario choice
- improve lesson summaries and rewards

Success criteria:
- starter tracks feel meaningfully different
- quiz interactions feel less repetitive

---

### F4. Plant inventory / greenhouse
**Goal:** Expand from one active companion into a collection loop.

Scope:
- unlock or own multiple plants
- switch active plant
- basic greenhouse/inventory screen
- persist per-plant progress and care state

Success criteria:
- multiple companions are manageable and worth collecting

---

### F5. Reward economy + cosmetics
**Goal:** Improve retention and motivation.

Scope:
- coins or similar soft currency
- cosmetic unlocks
- simple reward shop or unlock track
- better reward feedback

Success criteria:
- repeated actions feel rewarding
- rewards have visible purpose

---

### F6. Notifications / reminder loop
**Goal:** Improve retention and habit formation.

Scope:
- reminder notifications
- lesson-ready reminders
- care reminders
- streak-warning reminders

Success criteria:
- reminders are useful, not spammy

---

### F7. Companion personality system
**Goal:** Increase charm and emotional attachment.

Scope:
- species-based personalities
- more reactive lines
- contextual moods and tone

Success criteria:
- companion voice feels distinct across starters

---

### F8. Real-world plant mode
**Goal:** Create a strong differentiator beyond a simple virtual pet app.

Scope:
- optional real plant tracking
- photo log or simple checklist
- tie digital companion to real care habits

Success criteria:
- app begins bridging digital and real plant care

---

### F9. Localization foundation + Korean/German support
**Goal:** Make GreenBuddy ready for real multilingual use without hardcoded-copy debt.

Scope:
- localization-ready refactor for user-facing copy
- reduce hardcoded text in UI where practical
- add Korean (`ko`) and German (`de`) support
- establish a maintainable structure for lesson/personality/reminder text

Success criteria:
- app can switch languages cleanly
- key user-facing surfaces support ko/de
- future content additions do not require large i18n rewrites

---

### F10. Weather + seasonal feedback
**Goal:** Connect GreenBuddy more strongly to the user’s real environment.

Scope:
- city/manual location selection first
- local weather + seasonal summary feedback
- starter-specific contextual advice based on weather/season
- optional Home card and future reminder integration

Success criteria:
- GreenBuddy gives context-aware environmental advice
- weather/season feedback feels helpful, not overly prescriptive

---

### F11. Haptic / feedback polish
**Goal:** Make key actions feel more satisfying and alive.

Scope:
- haptic feedback for important events
- lesson success feedback
- growth/evolution feedback
- care action success feedback
- optional sound later if the haptic layer works well

Success criteria:
- important actions feel more tactile and rewarding
- feedback remains selective and not annoying

---

### F12. Plant AI companion mode
**Goal:** Let the user talk to their plant and make the plant express its state proactively in a more lifelike way.

Scope:
- optional AI-driven companion conversation mode
- user can chat with their current plant companion
- the plant expresses mood, care state, growth stage, and environmental context proactively
- tie dialogue to existing app state first before introducing open-ended generation everywhere
- start with guardrailed/state-aware responses, then expand if it proves valuable

Success criteria:
- the companion feels more alive than static scripted copy
- the plant can reflect real app state in conversation
- the feature remains safe, understandable, and not too expensive/fragile for MVP+

#### F12 Phase 1: State-aware companion chat MVP
**Goal:** Ship a safe, useful first version of plant conversation without depending on full freeform AI.

Scope:
- add a small companion chat entry point from Home
- create a companion state snapshot using current app state
  - active starter
  - personality
  - mood / health
  - care state
  - growth stage
  - daily missions
  - weather / season
  - real-plant mode summary
- support a small set of user intents first
  - status check
  - care advice
  - mission help
  - growth question
  - weather question
  - light casual chat
- responses should be state-aware and personality-aware
- prefer constrained/template or intent-driven responses over fully open-ended AI for this phase

Success criteria:
- the user can ask their plant simple questions and get responses that reflect real app state
- different starters feel distinct in conversation
- the system stays stable, understandable, and affordable to iterate on

## Current recommended implementation order
1. F1 Daily missions + streak system
2. F2 Growth and evolution thresholds
3. F3 Lesson content expansion + quiz variety
4. F4 Plant inventory / greenhouse
5. F5 Reward economy + cosmetics
6. F6 Notifications
7. F7 Companion personality system
8. F8 Real-world plant mode
9. F9 Localization foundation + Korean/German support
10. F10 Weather + seasonal feedback
11. F11 Haptic / feedback polish
12. F12 Plant AI companion mode

## Notes for reviewers
Reviewers should focus on:
- persistence correctness
- clear state ownership
- avoiding duplicate rewards
- UX clarity above the fold on phone screens
- preserving build health
- keeping features MVP-sized
