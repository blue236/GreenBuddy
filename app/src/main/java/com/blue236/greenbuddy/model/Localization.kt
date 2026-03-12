package com.blue236.greenbuddy.model

import java.util.Locale

fun normalizedLanguageTag(languageTag: String?): String = when (languageTag?.lowercase(Locale.ROOT)) {
    "de", "de-de", "de-at", "de-ch" -> "de"
    "ko", "ko-kr" -> "ko"
    else -> "en"
}

fun StarterPlantOption.localizedTitle(languageTag: String): String = when (id) {
    "monstera" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Monstera"
        "ko" -> "몬스테라"
        else -> "Monstera"
    }
    "basil" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Basilikum"
        "ko" -> "바질"
        else -> "Basil"
    }
    else -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Tomate"
        "ko" -> "토마토"
        else -> "Tomato"
    }
}

fun StarterPlantOption.localizedSubtitle(languageTag: String): String = when (id) {
    "monstera" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Entspannter Indoor-Starter"
        "ko" -> "부담 없이 시작하는 실내 식물"
        else -> "Easygoing indoor starter"
    }
    "basil" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Schnell wachsender Küchenbuddy"
        "ko" -> "빠르게 자라는 주방 친구"
        else -> "Fast-growing kitchen buddy"
    }
    else -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Lohnende Herausforderung mit Früchten"
        "ko" -> "열매까지 기대되는 도전형 식물"
        else -> "Rewarding fruiting challenge"
    }
}

fun StarterPlantOption.localizedCareTip(languageTag: String): String = when (id) {
    "monstera" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Wöchentlich drehen für gleichmäßiges Blattwachstum."
        "ko" -> "잎이 고르게 자라도록 매주 방향을 조금씩 바꿔 주세요."
        else -> "Rotate weekly for even leaf growth."
    }
    "basil" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Obere Blätter oft abknipsen, damit ich buschig bleibe."
        "ko" -> "윗잎을 자주 따 주면 더 풍성하게 자라요."
        else -> "Pinch top leaves often to keep me bushy."
    }
    else -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Mehr Sonne sorgt für kräftigere Stiele und süßere Früchte."
        "ko" -> "햇빛이 많을수록 줄기는 튼튼해지고 열매 맛도 좋아져요."
        else -> "More sun means stronger stems and sweeter fruit."
    }
}

fun StarterPlantOption.localizedStage(languageTag: String): String = when (companion.stage) {
    "Sprout" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Keimling"
        "ko" -> "새싹"
        else -> companion.stage
    }
    "Seedling" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Jungpflanze"
        "ko" -> "유묘"
        else -> companion.stage
    }
    "Starter" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Starter"
        "ko" -> "스타터"
        else -> companion.stage
    }
    else -> companion.stage
}

fun StarterPlantOption.localizedMood(languageTag: String): String = when (companion.mood) {
    "Curious" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Neugierig"
        "ko" -> "호기심 가득"
        else -> companion.mood
    }
    "Energetic" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Energiegeladen"
        "ko" -> "에너지 넘침"
        else -> companion.mood
    }
    "Ambitious" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Ehrgeizig"
        "ko" -> "의욕 가득"
        else -> companion.mood
    }
    else -> companion.mood
}

fun CareAction.localizedLabel(languageTag: String): String = when (this) {
    CareAction.WATER -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Gießen"
        "ko" -> "물 주기"
        else -> "Water"
    }
    CareAction.MOVE_TO_SUNLIGHT -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Mehr Sonne"
        "ko" -> "햇빛 쬐기"
        else -> "Sun bath"
    }
    CareAction.FERTILIZE -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Düngen"
        "ko" -> "영양 주기"
        else -> "Fertilize"
    }
}

fun RealPlantCareAction.localizedLabel(languageTag: String): String = when (this) {
    RealPlantCareAction.WATERED -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Meine echte Pflanze gegossen"
        "ko" -> "실제 식물에 물 줬어요"
        else -> "I watered my real plant"
    }
    RealPlantCareAction.CHECKED_LIGHT -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Licht geprüft"
        "ko" -> "빛 상태를 확인했어요"
        else -> "I checked its light"
    }
    RealPlantCareAction.FERTILIZED -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Meine echte Pflanze gedüngt"
        "ko" -> "실제 식물에 영양을 줬어요"
        else -> "I fed my real plant"
    }
}

fun PlantCareState.localizedHealth(languageTag: String): String = when (health) {
    "Thriving" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Prächtig"
        "ko" -> "아주 잘 자라는 중"
        else -> health
    }
    "Healthy" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Gesund"
        "ko" -> "건강함"
        else -> health
    }
    "Stable" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Stabil"
        "ko" -> "안정적"
        else -> health
    }
    else -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Braucht Aufmerksamkeit"
        "ko" -> "돌봄이 필요해요"
        else -> health
    }
}

fun PlantCareState.localizedMood(languageTag: String): String = when (mood) {
    "Thirsty" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Durstig"
        "ko" -> "목말라요"
        else -> mood
    }
    "Shady" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Lichtarm"
        "ko" -> "빛이 부족해요"
        else -> mood
    }
    "Hungry" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Hungrig"
        "ko" -> "영양이 부족해요"
        else -> mood
    }
    "Joyful" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Fröhlich"
        "ko" -> "아주 기분 좋아요"
        else -> mood
    }
    "Content" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Zufrieden"
        "ko" -> "만족스러워요"
        else -> mood
    }
    else -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Schläfrig"
        "ko" -> "조금 처져 있어요"
        else -> mood
    }
}

fun CosmeticItem.localizedName(languageTag: String): String = when (id) {
    "classic_pot" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Klassischer Tontopf"
        "ko" -> "클래식 토분"
        else -> name
    }
    "sunny_ribbon" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Sonniges Band"
        "ko" -> "햇살 리본"
        else -> name
    }
    else -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Goldener Schimmer"
        "ko" -> "골든 글로우"
        else -> name
    }
}

fun CosmeticItem.localizedDescription(languageTag: String): String = when (id) {
    "classic_pot" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Ein warmer Terrakottatopf für gemütliche Fensterbank-Vibes."
        "ko" -> "포근한 창가 분위기를 만드는 따뜻한 테라코타 화분이에요."
        else -> description
    }
    "sunny_ribbon" -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Ein fröhliches Band, das deinen Buddy besonders gepflegt aussehen lässt."
        "ko" -> "버디를 더 정성껏 돌본 것처럼 보이게 해 주는 밝은 리본이에요."
        else -> description
    }
    else -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Ein funkelnder Effekt für Begleiter mit heißer Erfolgsserie."
        "ko" -> "연속 성장 중인 버디를 빛나게 해 주는 반짝 효과예요."
        else -> description
    }
}

fun unlockRequirementFor(option: StarterPlantOption, ownedStarterIds: Set<String>, languageTag: String): String = when {
    option.id in ownedStarterIds -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Bereit in deinem Gewächshaus"
        "ko" -> "온실에서 바로 사용할 수 있어요"
        else -> "Ready in your greenhouse"
    }
    option.id == nextUnlockableStarterId(ownedStarterIds) -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Wird automatisch freigeschaltet, sobald du einen aktuellen Pflanzenpfad abschließt."
        "ko" -> "현재 식물 트랙 하나를 완료하면 자동으로 잠금 해제돼요."
        else -> "Automatically unlocks when you complete any current plant track."
    }
    else -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Schalte zuerst frühere Gewächshaus-Begleiter frei."
        "ko" -> "먼저 앞선 온실 친구들을 해제해 주세요."
        else -> "Unlock earlier greenhouse companions first."
    }
}
