package com.example.domain.model

data class UserPreferences(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val theme: AppTheme = AppTheme.SYSTEM,
    val locationIntervalSeconds: Int = 60
)

enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    TURKISH("tr")
}

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}
