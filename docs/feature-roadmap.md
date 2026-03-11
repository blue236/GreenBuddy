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

## Current recommended implementation order
1. F1 Daily missions + streak system
2. F2 Growth and evolution thresholds
3. F3 Lesson content expansion + quiz variety
4. F4 Plant inventory / greenhouse
5. F5 Reward economy + cosmetics
6. F6 Notifications
7. F7 Companion personality system
8. F8 Real-world plant mode

## Notes for reviewers
Reviewers should focus on:
- persistence correctness
- clear state ownership
- avoiding duplicate rewards
- UX clarity above the fold on phone screens
- preserving build health
- keeping features MVP-sized
