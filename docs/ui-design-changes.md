# GreenBuddy UI/UX 재설계 — 디자인 변경사항 문서

> **브랜치:** `claude/design-greenbuddy-ui-Qlei2`
> **목표:** 귀엽고 친근한(cute & friendly) 비주얼 아이덴티티 구축

---

## 배경 및 동기

기존 UI는 Material3 기본값을 그대로 사용했으며, 커스텀 컬러/타입/쉐이프가 전혀 없었습니다. 네비게이션 아이콘은 `"●"/"○"` 텍스트로 처리됐고, 동반자 히어로 카드가 스크롤 9번째에 위치하는 등 시각적 계층 구조가 없었습니다. 이번 재설계는 GreenBuddy만의 따뜻하고 생동감 있는 디자인 언어를 정립합니다.

---

## 1. 디자인 시스템 (`ui/theme/Theme.kt`)

### 1.1 컬러 팔레트

| 역할 | 색상 | 헥스값 | 용도 |
|------|------|--------|------|
| Primary | 숲 초록 | `#4B8B5E` | CTA 버튼, 활성 상태, 완료 체크 |
| PrimaryContainer | 연한 민트 | `#C8E6C9` | 선택 카드 배경, 히어로 카드 그라디언트 시작 |
| Secondary | 풀잎 초록 | `#93C572` | 서브 액션, 현재 레슨 노드 |
| SecondaryContainer | 연한 라임 | `#DCEDC8` | 미션 강조, 칩 배경 |
| Tertiary | 따뜻한 앰버 | `#F5A623` | 보상/XP 순간, 스트릭 뱃지 |
| TertiaryContainer | 연한 골드 | `#FFF3E0` | 보상 카드, 현재 선택 미리보기 |
| Background | 초록빛 흰색 | `#F3FBF4` | 앱 전체 배경 |
| SurfaceContainer | 연한 초록 | `#E7F5E8` | 섹션 구분 배경 |
| Error | 따뜻한 로즈 | `#C0392B` | 오답, 경고 상태 |

**GreenBuddyColors 특수 토큰 (Material3 슬롯 외 추가):**

| 토큰 | 헥스값 | 용도 |
|------|--------|------|
| `stem` | `#2E5D3A` | 장식 요소 |
| `leafGold` | `#F5A623` | 🍃 잎 토큰 금액 텍스트 |
| `streakFlame` | `#FF6B35` | 🔥 스트릭 카운트 |
| `companionBubble` | `#F0FBF0` | 동반자 말풍선 배경 |
| `userBubble` | `#E8F5E8` | 사용자 말풍선 배경 |

### 1.2 타입 스케일 (GreenBuddyTypography)

Nunito 호환 비율로 설계된 15단계 스케일. SemiBold/Bold 계열로 귀엽고 선명한 텍스트 느낌을 구현합니다.

```
Display Large  57sp ExtraBold  → 스플래시/업적 팝업
Headline Large 32sp Bold       → 화면 제목
Title Medium   16sp SemiBold   → 카드 제목, 섹션 헤딩
Body Medium    14sp Regular    → 본문 텍스트
Label Medium   12sp SemiBold   → 칩, 뱃지, 버튼
```

### 1.3 쉐이프 시스템 (GreenBuddyShapes)

모든 모서리가 둥글어 장난감처럼 친근한 느낌을 줍니다.

| 단계 | 반경 | 사용처 |
|------|------|--------|
| ExtraSmall | 4dp | 툴팁 |
| Small | 8dp | 입력 필드 |
| **Medium** | **16dp** | **일반 카드** (기본값) |
| **Large** | **24dp** | **히어로 카드, 바텀 시트** |
| **ExtraLarge** | **32dp** | **동반자 컨테이너, 온보딩** |
| Full | CircleShape | 버튼, FAB, 아이콘 버블, 네비게이션 인디케이터 |

---

## 2. 신규 컴포넌트

기존 `AppComponents.kt` 1개 파일에서 **5개 파일로 분리 확장**했습니다.

### 2.1 AppComponents.kt (기존 → 확장)

| 컴포넌트 | 설명 |
|----------|------|
| `GreenBuddyCard` | 테마 기반 기본 카드 래퍼 (클릭 가능 변형 포함) |
| `GreenBuddyHeroCard` | PrimaryContainer→SurfaceContainer 선형 그라디언트 히어로 카드, ExtraLarge 쉐이프 |
| `SectionTitle` | 섹션 제목 + trailing 슬롯 (뱃지 등 삽입 가능) |
| `GreenBuddyButton` | Primary / Secondary / Ghost 3종 변형, CircleShape 완전 둥근 버튼 |
| `GreenBuddyChip` | 이모지 슬롯 + selected 상태(SecondaryContainer 배경) 칩 |
| `GreenBuddyTopBar` | 설정 화면용 TopAppBar (배경 Background 색상, 플랫 디자인) |
| `StatCard` | 기존 컴포넌트 — `Medium` 쉐이프 및 1dp 고도로 업데이트 |
| `StarterPlantCard` | 기존 컴포넌트 — 72dp 이모지 박스, `Large` 쉐이프, 선택 시 Primary 테두리+체크 원형 |

### 2.2 CompanionComponents.kt (신규)

| 컴포넌트 | 설명 |
|----------|------|
| `CompanionAvatarBubble` | 감정별 컬러 링 + 2.2초 주기 Y축 ±4dp 부유 애니메이션 이모지 버블 |
| `EmotionBanner` | 감정 상태 + 친밀도 라벨을 Pill 배너로 표시, 감정별 컨테이너 색상 |
| `CareActionButton` | 케어 액션 타일 (이모지 원형 + 라벨 수직 배치), 기존 `AssistChip` 대체 |
| `CareStatBar` | 수화/햇빛/영양 수치 바 (0-33: 오류, 34-66: 경고, 67+: 정상 색상 자동 변경) |

### 2.3 MissionComponents.kt (신규)

| 컴포넌트 | 설명 |
|----------|------|
| `MissionRowItem` | 완료: Primary 색상 체크 원형 / 다음 미션: SecondaryContainer 강조 배경 |
| `StreakBadge` | `🔥 N days` — TertiaryContainer Pill, `streakFlame` 색상 |

### 2.4 RewardComponents.kt (신규)

| 컴포넌트 | 설명 |
|----------|------|
| `LeafTokenDisplay` | `🍃 {금액}` — leafGold 색상, Normal/Large 2가지 크기 |
| `WalletHeader` | Display Small 크기 토큰 잔액 + 장착 코스메틱 표시 |
| `CosmeticShopCard` | **4가지 상태**: Equipped(Primary), Affordable(TertiaryContainer + 금색 테두리), Owned, Unowned(잠금) |

### 2.5 PlantComponents.kt (신규)

| 컴포넌트 | 설명 |
|----------|------|
| `PlantInventoryCard` | 활성(Primary 테두리 + 닷), 소유(SurfaceVariant), 잠금(🔒 오버레이 + 스크림) 3상태 |
| `QuizOptionTile` | Idle/Selected/Correct/Incorrect 4상태 — `animateColorAsState` + scale spring 애니메이션 |
| `LessonPathNode` | 완료/현재/미래 노드 시각화, 현재 노드는 더 큰 원형 + Bold 텍스트 |

---

## 3. 화면별 변경사항

### 3.1 네비게이션 바 (`GreenBuddyApp.kt`)

**Before:**
```kotlin
icon = { Text(if (selected) "●" else "○") }
```

**After:**
```kotlin
icon = { Icon(imageVector = tab.icon, ...) }
// HOME=Outlined.Home, LEARN=Outlined.MenuBook,
// DEX=Outlined.LocalFlorist, PROFILE=Outlined.Person
```

- 선택 인디케이터: PrimaryContainer Pill 모양
- 선택 아이콘/라벨: Primary 색상 / 미선택: OnSurfaceVariant

---

### 3.2 홈 화면 (`HomeScreen.kt`)

**카드 순서 완전 재배치:**

| 기존 순서 | 새로운 순서 |
|-----------|-------------|
| 1. 텍스트 제목 | 1. 상단 스트립 (제목 + 🍃 토큰) |
| 2. 부제목 | 2. **동반자 히어로 카드** ← 9번째에서 이동 |
| 3. 오늘의 레슨 | 3. 케어 액션 타일 3개 |
| 4. 성장 카드 | 4. 일일 미션 + 🔥 스트릭 뱃지 |
| 5. 미션 카드 | 5. 오늘의 레슨 넛지 |
| 6-8. 보상 스트립 | 6. 접기/펼치기 토글 |
| **9. 동반자 히어로** | 7. (접힘) 성장/보상/날씨/실물 모드 |
| 10. 케어 AssistChip |  |

**케어 액션 개선:**
- `FlowRow { AssistChip × 3 }` → `Row { CareActionButton × 3 (weight(1f))}`
- 각 타일: 48dp 이모지 원형 + 라벨, tappable 카드

**HomeHeroCard 내부 개선:**
- `StatCard` → `GreenBuddyHeroCard` (그라디언트 배경)
- 이모지 텍스트 → `CompanionAvatarBubble` (부유 애니메이션 + 감정 링)
- 감정/친밀도 AssistChip → `EmotionBanner` (Pill 배너)
- 베스트 액션 버튼 → `GreenBuddyButton`
- 채팅 토글 → `GreenBuddyButton` Secondary 변형

---

### 3.3 학습 화면 (`LearnScreen.kt`)

| 영역 | 변경사항 |
|------|----------|
| 레슨 히어로 카드 | `Card(PrimaryContainer)` → `GreenBuddyHeroCard` (그라디언트) |
| 학습 경로 | 원형 박스 인라인 코드 → `LessonPathNode` 컴포넌트 |
| 퀴즈 선택지 | 인라인 `Card` → `QuizOptionTile` (4상태 애니메이션) |
| 바텀 액션 바 | `topStart/topEnd = 24dp` → `32dp` (ExtraLarge, 더 둥글게) |
| 정답/오답 피드백 | 기존 텍스트 색상 → Primary(정답) / Error(오답) 색상 유지, 퀴즈 타일에 시각적 애니메이션 추가 |

---

### 3.4 온실 화면 (`DexScreen.kt`)

| 변경사항 | 설명 |
|----------|------|
| 요약 카드 | `Card(PrimaryContainer)` → `GreenBuddyHeroCard` |
| 식물 목록 | 인라인 `Card` with 하드코딩 색상 → `PlantInventoryCard` |
| 잠금 처리 | 없음 → 이모지 영역에 🔒 오버레이 Badge |
| 활성 식물 | 텍스트만 표시 → Primary 색상 2dp 테두리 + Primary 닷 뱃지 |
| 색상 | `Color(0xFFE8F5E9)` 등 하드코딩 → 전부 테마 토큰 |

---

### 3.5 프로필 화면 (`ProfileScreen.kt`)

| 변경사항 | 설명 |
|----------|------|
| 상단 히어로 | 없음 → `GreenBuddyHeroCard`: 동반자 이모지, XP, 스트릭, `LeafTokenDisplay` (Large) |
| 보상 숍 | `CosmeticShopRow` (단순 Row) → `CosmeticShopCard` (4상태 카드형) |
| 섹션 구조 | 나열식 `StatCard` → `SectionTitle` + 카드 배치 |

---

### 3.6 온보딩 화면 (`OnboardingScreen.kt`)

| 변경사항 | 설명 |
|----------|------|
| 상단 타이틀 | `Text` 2줄 → `GreenBuddyHeroCard` (🌿 + 제목 + 부제목) |
| "How it works" | `Card(Color(0xFFE8F5E9))` 하드코딩 → `GreenBuddyCard(SurfaceContainer)` |
| 스타터 선택 | `Column { Text("Choose...") }` → `SectionTitle` |
| 현재 선택 미리보기 | `StatCard` → `GreenBuddyCard(TertiaryContainer)` (따뜻한 골드 배경) |
| CTA 버튼 | `Button(fillMaxWidth)` → `GreenBuddyButton(Primary, fillMaxWidth)` (CircleShape) |

---

## 4. 애니메이션 명세

| 위치 | 애니메이션 | API |
|------|-----------|-----|
| 동반자 이모지 | Y축 0 ↔ -4dp, 1.1초 주기 반복 | `infiniteTransition + animateFloat` |
| 동반자 감정 링 | 감정 변경 시 색상 전환 600ms | `animateColorAsState(tween(600))` |
| 케어 버튼 | 탭 영역 ripple (Material 기본) | `clickable` |
| 퀴즈 선택 | 배경색 전환 200ms + scale 1.02x spring | `animateColorAsState + animateFloatAsState(spring)` |
| 퀴즈 정답/오답 | PrimaryContainer/ErrorContainer 색상 전환 | `animateColorAsState(tween(200))` |
| 잎 토큰 | `LeafTokenDisplay` 금색 강조 | `GreenBuddyColors.leafGold` |

---

## 5. 의존성 변경 (`app/build.gradle.kts`)

```kotlin
// 추가됨
implementation("androidx.compose.material:material-icons-extended")
// 네비게이션 바 Material Icons Outlined 사용
```

---

## 6. 파일 변경 목록

| 파일 | 상태 | 설명 |
|------|------|------|
| `ui/theme/Theme.kt` | **수정** | 전면 재작성 — 커스텀 컬러/타입/쉐이프 |
| `ui/components/AppComponents.kt` | **수정** | 기존 유지 + 신규 6개 컴포넌트 추가 |
| `ui/components/CompanionComponents.kt` | **신규** | 동반자 관련 컴포넌트 |
| `ui/components/MissionComponents.kt` | **신규** | 미션/스트릭 컴포넌트 |
| `ui/components/RewardComponents.kt` | **신규** | 보상/코스메틱 컴포넌트 |
| `ui/components/PlantComponents.kt` | **신규** | 식물/퀴즈/레슨 경로 컴포넌트 |
| `ui/GreenBuddyApp.kt` | **수정** | 네비게이션 바 아이콘 교체 |
| `ui/screens/HomeScreen.kt` | **수정** | 카드 재배치, CareActionButton, 히어로 개선 |
| `ui/screens/LearnScreen.kt` | **수정** | QuizOptionTile, LessonPathNode, 바텀 바 |
| `ui/screens/DexScreen.kt` | **수정** | PlantInventoryCard, 테마 토큰, 히어로 카드 |
| `ui/screens/ProfileScreen.kt` | **수정** | 히어로 카드, CosmeticShopCard |
| `ui/screens/OnboardingScreen.kt` | **수정** | GreenBuddyHeroCard, GreenBuddyButton |
| `app/build.gradle.kts` | **수정** | material-icons-extended 추가 |

---

## 7. 향후 개선 가능 사항

- **Nunito 폰트 적용**: `res/font/` 디렉토리에 Nunito ttf 파일 추가 시 현재 타입 스케일 즉시 적용 가능
- **다크 모드**: `GreenBuddyLightColors` 옆에 `GreenBuddyDarkColors` 추가로 확장 가능
- **성장 해제 애니메이션**: 카드 scale/alpha 등장 + 이모지 회전 구현 (confetti 효과 포함)
- **케어 버튼 햅틱**: press 시 `scale 0.92 → 1.0` spring 애니메이션 추가
- **미션 체크 애니메이션**: 완료 시 원형 Primary 색상 변경 + 바운스 효과
