package com.trendpulse.app

enum class TrendType(val label: String) {
    WEATHER("Weather Trend"),
    MARKET("Market Trend"),
    MUSIC("Music Trend"),
    MOVIES("Movies Trend"),
    PODCAST("Podcast Trend");

    companion object {
        fun fromName(name: String?): TrendType =
            entries.firstOrNull { it.name == name } ?: WEATHER
    }
}
