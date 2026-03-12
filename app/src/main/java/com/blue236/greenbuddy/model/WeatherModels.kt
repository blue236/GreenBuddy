package com.blue236.greenbuddy.model

import java.time.LocalDate
import java.time.Month

enum class WeatherSeason {
    SPRING,
    SUMMER,
    AUTUMN,
    WINTER,
}

enum class WeatherCondition {
    COOL_BRIGHT,
    WARM_SUNNY,
    HOT_DRY,
    MILD_HUMID,
    COLD_DIM,
}

data class WeatherCityOption(
    val id: String,
    val defaultName: String,
    val climateBySeason: Map<WeatherSeason, WeatherCondition>,
)

data class WeatherSnapshot(
    val city: WeatherCityOption,
    val season: WeatherSeason,
    val condition: WeatherCondition,
)

data class WeatherAdvice(
    val summary: String,
    val starterAdvice: String,
    val reminderHint: String,
)

object WeatherCatalog {
    val cityOptions = listOf(
        WeatherCityOption(
            id = "berlin",
            defaultName = "Berlin",
            climateBySeason = mapOf(
                WeatherSeason.SPRING to WeatherCondition.COOL_BRIGHT,
                WeatherSeason.SUMMER to WeatherCondition.WARM_SUNNY,
                WeatherSeason.AUTUMN to WeatherCondition.COOL_BRIGHT,
                WeatherSeason.WINTER to WeatherCondition.COLD_DIM,
            ),
        ),
        WeatherCityOption(
            id = "seoul",
            defaultName = "Seoul",
            climateBySeason = mapOf(
                WeatherSeason.SPRING to WeatherCondition.COOL_BRIGHT,
                WeatherSeason.SUMMER to WeatherCondition.MILD_HUMID,
                WeatherSeason.AUTUMN to WeatherCondition.WARM_SUNNY,
                WeatherSeason.WINTER to WeatherCondition.COLD_DIM,
            ),
        ),
        WeatherCityOption(
            id = "barcelona",
            defaultName = "Barcelona",
            climateBySeason = mapOf(
                WeatherSeason.SPRING to WeatherCondition.WARM_SUNNY,
                WeatherSeason.SUMMER to WeatherCondition.HOT_DRY,
                WeatherSeason.AUTUMN to WeatherCondition.WARM_SUNNY,
                WeatherSeason.WINTER to WeatherCondition.COOL_BRIGHT,
            ),
        ),
    )

    fun cityById(id: String?): WeatherCityOption =
        cityOptions.firstOrNull { it.id == id } ?: cityOptions.first()
}

interface WeatherProvider {
    fun snapshotFor(cityId: String, date: LocalDate = LocalDate.now()): WeatherSnapshot
}

object SeasonalWeatherProvider : WeatherProvider {
    override fun snapshotFor(cityId: String, date: LocalDate): WeatherSnapshot {
        val city = WeatherCatalog.cityById(cityId)
        val season = date.toWeatherSeason()
        return WeatherSnapshot(
            city = city,
            season = season,
            condition = city.climateBySeason[season] ?: WeatherCondition.COOL_BRIGHT,
        )
    }
}

object WeatherAdviceGenerator {
    fun adviceFor(starter: StarterPlantOption, snapshot: WeatherSnapshot, languageTag: String = "en"): WeatherAdvice {
        val lang = normalizedLanguageTag(languageTag)
        return WeatherAdvice(
            summary = summaryFor(snapshot, lang),
            starterAdvice = starterAdviceFor(starter.id, snapshot, lang),
            reminderHint = reminderHintFor(snapshot, lang),
        )
    }

    private fun summaryFor(snapshot: WeatherSnapshot, lang: String): String {
        val seasonLabel = snapshot.season.localizedLabel(lang)
        val conditionLabel = snapshot.condition.localizedLabel(lang)
        return when (lang) {
            "de" -> "${snapshot.city.localizedName(lang)} ist gerade $seasonLabel mit eher $conditionLabel Bedingungen."
            "ko" -> "${snapshot.city.localizedName(lang)}는 지금 ${seasonLabel}이고, 전반적으로 $conditionLabel 환경이에요."
            else -> "${snapshot.city.localizedName(lang)} is in $seasonLabel right now with mostly $conditionLabel conditions."
        }
    }

    private fun starterAdviceFor(starterId: String, snapshot: WeatherSnapshot, lang: String): String {
        return when (starterId) {
            "monstera" -> monsteraAdvice(snapshot, lang)
            "basil" -> basilAdvice(snapshot, lang)
            "tomato" -> tomatoAdvice(snapshot, lang)
            else -> genericAdvice(snapshot, lang)
        }
    }

    private fun reminderHintFor(snapshot: WeatherSnapshot, lang: String): String = when (snapshot.condition) {
        WeatherCondition.COLD_DIM -> when (lang) {
            "de" -> "Guter Kandidat für spätere Winter-Licht- oder Rotations-Erinnerungen."
            "ko" -> "나중에 겨울철 빛 점검이나 화분 위치 조정 알림으로 연결하기 좋아요."
            else -> "Good candidate for a future winter light-check or rotation reminder."
        }
        WeatherCondition.HOT_DRY -> when (lang) {
            "de" -> "Später könnte hier eine Hitzewellen-Erinnerung zum Feuchte-Check helfen."
            "ko" -> "나중에 무더위 수분 점검 알림으로 확장하기 좋은 패턴이에요."
            else -> "This would translate well into a future heat-wave moisture reminder."
        }
        else -> when (lang) {
            "de" -> "Praktisch als sanfter saisonaler Home-Hinweis — noch ohne Pflichtgefühl."
            "ko" -> "부담 없이 보여 주는 계절성 홈 힌트로 쓰기 좋아요."
            else -> "Best used as a lightweight seasonal nudge on Home for now."
        }
    }

    private fun monsteraAdvice(snapshot: WeatherSnapshot, lang: String): String = when (snapshot.condition) {
        WeatherCondition.COLD_DIM -> when (lang) {
            "de" -> "Monstera mag den Winter lieber hell, aber nicht zugig. Halte sie näher ans Fenster und gieße etwas zurückhaltender."
            "ko" -> "몬스테라는 겨울에 바람은 피하면서도 더 밝은 자리를 좋아해요. 창가에 조금 더 가깝게 두고 물은 약간만 줄여 주세요."
            else -> "Monstera handles winter best with brighter light and less draft. Move it a little closer to the window and ease back on watering."
        }
        WeatherCondition.WARM_SUNNY, WeatherCondition.COOL_BRIGHT -> when (lang) {
            "de" -> "Das passt gut für Monstera. Helles indirektes Licht hält das Wachstum stabil — jetzt ist ein guter Moment zum Drehen der Pflanze."
            "ko" -> "몬스테라에게 잘 맞는 시기예요. 밝은 간접광을 유지하고, 지금처럼 빛이 고를 때 화분을 한 번 돌려 주세요."
            else -> "This is a comfortable stretch for Monstera. Keep the bright indirect light steady and use the season to rotate the pot for even growth."
        }
        else -> when (lang) {
            "de" -> "Wenn es wärmer oder feuchter wird, achte auf weichere Blätter und lass die oberste Erde leicht antrocknen, bevor du wieder gießt."
            "ko" -> "따뜻하거나 습해질수록 잎이 무르게 느껴지지 않는지 살피고, 겉흙이 살짝 말랐을 때 물을 주세요."
            else -> "As conditions turn warmer or more humid, watch for softer leaves and let the top layer of soil dry slightly before the next watering."
        }
    }

    private fun basilAdvice(snapshot: WeatherSnapshot, lang: String): String = when (snapshot.condition) {
        WeatherCondition.COLD_DIM -> when (lang) {
            "de" -> "Basilikum wird in dunklen, kalten Wochen schnell lang und blass. Such das sonnigste Fenster und warte mit starkem Wachstum eher auf den Frühling."
            "ko" -> "바질은 춥고 어두우면 금방 웃자라요. 가장 햇볕이 좋은 창가를 찾고, 본격 성장은 봄빛이 돌아올 때 기대하는 편이 좋아요."
            else -> "Basil struggles in cold, dim weeks. Give it your sunniest window and expect steadier growth once spring light returns."
        }
        WeatherCondition.HOT_DRY -> when (lang) {
            "de" -> "Heiße, trockene Tage machen Basilikum durstig. Prüfe die Erde öfter und ernte die Spitzen regelmäßig, damit die Pflanze kompakt bleibt."
            "ko" -> "덥고 건조한 날에는 바질이 금방 목말라해요. 흙 상태를 더 자주 보고, 윗순을 자주 따서 수형을 촘촘하게 유지해 주세요."
            else -> "Hot, dry weather makes basil thirsty fast. Check the soil more often and pinch the top growth regularly to keep it bushy."
        }
        else -> when (lang) {
            "de" -> "Basilikum sollte jetzt Tempo aufnehmen. Viel Licht und gleichmäßige Feuchte helfen, damit die Blätter zart und aromatisch bleiben."
            "ko" -> "지금은 바질이 속도를 내기 좋은 시기예요. 빛을 충분히 주고 수분을 고르게 유지하면 잎이 부드럽고 향도 좋아져요."
            else -> "Basil should pick up speed in these conditions. Strong light and steady moisture will keep the leaves tender and flavorful."
        }
    }

    private fun tomatoAdvice(snapshot: WeatherSnapshot, lang: String): String = when (snapshot.condition) {
        WeatherCondition.COLD_DIM -> when (lang) {
            "de" -> "Tomaten brauchen mehr Sonne als der Winter meist liefert. Halte Erwartungen klein, vermeide Staunässe und plane das kräftige Wachstum für hellere Wochen."
            "ko" -> "토마토는 겨울이 주는 빛보다 더 많은 햇빛을 원해요. 지금은 무리하게 키우기보다 과습을 피하고 밝은 계절을 준비하는 편이 좋아요."
            else -> "Tomatoes want more sun than winter usually gives. Keep expectations modest, avoid soggy soil, and treat this as setup time for brighter weeks."
        }
        WeatherCondition.WARM_SUNNY, WeatherCondition.HOT_DRY -> when (lang) {
            "de" -> "Das ist Tomatenwetter. Gib der Pflanze möglichst volle Sonne und prüfe früh, ob Stütze und gleichmäßiges Gießen mit dem Wachstum mithalten."
            "ko" -> "토마토가 좋아하는 날씨예요. 가능한 한 햇빛을 많이 주고, 지지대와 규칙적인 물주기가 성장 속도를 따라가는지 미리 확인해 주세요."
            else -> "This is tomato weather. Push for as much sun as you can and make sure support plus consistent watering keep up with the growth spurt."
        }
        else -> when (lang) {
            "de" -> "Milde Übergangswochen sind gut zum Abhärten und Beobachten. Mehr Licht und Luftbewegung helfen, bevor die Pflanze richtig losschiebt."
            "ko" -> "완만한 환절기는 적응 상태를 보기 좋아요. 본격 성장 전에 빛과 통풍을 조금 더 챙겨 주면 도움이 됩니다."
            else -> "Mild shoulder-season weather is good for observation and setup. A little more light and airflow now will support the next growth push."
        }
    }

    private fun genericAdvice(snapshot: WeatherSnapshot, lang: String): String {
        @Suppress("UNUSED_VARIABLE") val unusedSnapshot = snapshot
        return when (lang) {
        "de" -> "Passe Licht, Wasser und Geduld leicht an die Saison an, statt gegen sie zu arbeiten."
        "ko" -> "계절을 거스르기보다 빛과 물, 기대치를 조금씩 맞춰 가는 편이 좋아요."
        else -> "Use the season to adjust light, water, and expectations instead of fighting it."
        }
    }
}

fun LocalDate.toWeatherSeason(): WeatherSeason = when (month) {
    Month.DECEMBER, Month.JANUARY, Month.FEBRUARY -> WeatherSeason.WINTER
    Month.MARCH, Month.APRIL, Month.MAY -> WeatherSeason.SPRING
    Month.JUNE, Month.JULY, Month.AUGUST -> WeatherSeason.SUMMER
    Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER -> WeatherSeason.AUTUMN
}

fun WeatherCityOption.localizedName(@Suppress("UNUSED_PARAMETER") languageTag: String): String = defaultName

private fun WeatherSeason.localizedLabel(languageTag: String): String = when (normalizedLanguageTag(languageTag)) {
    "de" -> when (this) {
        WeatherSeason.SPRING -> "Frühling"
        WeatherSeason.SUMMER -> "Sommer"
        WeatherSeason.AUTUMN -> "Herbst"
        WeatherSeason.WINTER -> "Winter"
    }
    "ko" -> when (this) {
        WeatherSeason.SPRING -> "봄"
        WeatherSeason.SUMMER -> "여름"
        WeatherSeason.AUTUMN -> "가을"
        WeatherSeason.WINTER -> "겨울"
    }
    else -> name.lowercase()
}

private fun WeatherCondition.localizedLabel(languageTag: String): String = when (normalizedLanguageTag(languageTag)) {
    "de" -> when (this) {
        WeatherCondition.COOL_BRIGHT -> "kühlen, hellen"
        WeatherCondition.WARM_SUNNY -> "warmen, sonnigen"
        WeatherCondition.HOT_DRY -> "heißen, trockenen"
        WeatherCondition.MILD_HUMID -> "milden, feuchten"
        WeatherCondition.COLD_DIM -> "kalten, dunkleren"
    }
    "ko" -> when (this) {
        WeatherCondition.COOL_BRIGHT -> "선선하고 밝은"
        WeatherCondition.WARM_SUNNY -> "따뜻하고 화창한"
        WeatherCondition.HOT_DRY -> "덥고 건조한"
        WeatherCondition.MILD_HUMID -> "온화하고 습한"
        WeatherCondition.COLD_DIM -> "춥고 어두운"
    }
    else -> when (this) {
        WeatherCondition.COOL_BRIGHT -> "cool, bright"
        WeatherCondition.WARM_SUNNY -> "warm, sunny"
        WeatherCondition.HOT_DRY -> "hot, dry"
        WeatherCondition.MILD_HUMID -> "mild, humid"
        WeatherCondition.COLD_DIM -> "cold, dim"
    }
}
