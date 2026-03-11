package com.blue236.greenbuddy.model

import java.util.Locale

enum class AppLanguage(val storageValue: String, val languageTag: String?) {
    SYSTEM("system", null),
    ENGLISH("en", "en"),
    GERMAN("de", "de"),
    KOREAN("ko", "ko");

    companion object {
        fun fromStorageValue(value: String?): AppLanguage = entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}

fun AppLanguage.effectiveLanguageTag(systemLanguageTag: String?): String =
    languageTag ?: normalizedLanguageTag(systemLanguageTag)

fun localizedLanguageName(option: AppLanguage, languageTag: String): String = when (option) {
    AppLanguage.SYSTEM -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Systemstandard"
        "ko" -> "시스템 기본값"
        else -> "System default"
    }
    AppLanguage.ENGLISH -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Englisch"
        "ko" -> "영어"
        else -> "English"
    }
    AppLanguage.GERMAN -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Deutsch"
        "ko" -> "독일어"
        else -> "German"
    }
    AppLanguage.KOREAN -> when (normalizedLanguageTag(languageTag)) {
        "de" -> "Koreanisch"
        "ko" -> "한국어"
        else -> "Korean"
    }
}

fun systemLanguageLabel(option: AppLanguage, systemLanguageTag: String?, languageTag: String): String {
    if (option != AppLanguage.SYSTEM) return localizedLanguageName(option, languageTag)
    val systemName = when (normalizedLanguageTag(systemLanguageTag)) {
        "de" -> localizedLanguageName(AppLanguage.GERMAN, languageTag)
        "ko" -> localizedLanguageName(AppLanguage.KOREAN, languageTag)
        else -> localizedLanguageName(AppLanguage.ENGLISH, languageTag)
    }
    return when (normalizedLanguageTag(languageTag)) {
        "de" -> "${localizedLanguageName(option, languageTag)} ($systemName)"
        "ko" -> "${localizedLanguageName(option, languageTag)} ($systemName)"
        else -> "${localizedLanguageName(option, languageTag)} ($systemName)"
    }
}

fun String.toAppLanguageOrSystem(): AppLanguage = AppLanguage.fromStorageValue(lowercase(Locale.ROOT))
