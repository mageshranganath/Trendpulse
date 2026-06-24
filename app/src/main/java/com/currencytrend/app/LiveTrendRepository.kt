package com.trendpulse.app

import com.trendpulse.app.models.CurrencyRate
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URLEncoder

class LiveTrendRepository {

    data class WeatherTrendResult(
        val historical: List<CurrencyRate>,
        val forecast: List<CurrencyRate>
    )

    private val client = OkHttpClient.Builder().build()

    suspend fun fetch(
        type: TrendType,
        countryCode: String,
        languageCode: String,
        genreId: String,
        locationQuery: String,
        days: Int
    ): List<CurrencyRate> = withContext(Dispatchers.IO) {
        when (type) {
            TrendType.WEATHER -> fetchWeather(countryCode, locationQuery, days).historical
            TrendType.MARKET -> fetchMarket(days, genreId, countryCode)
            TrendType.MUSIC -> fetchMedia("musicTrack", countryCode, languageCode, genreId)
            TrendType.MOVIES -> fetchMovieItems(countryCode, languageCode, genreId).mapIndexed { index, item ->
                CurrencyRate(item.title, item.score)
            }
            TrendType.PODCAST -> fetchMedia("podcast", countryCode, languageCode, genreId)
        }
    }

    suspend fun fetchWeatherBundle(
        countryCode: String,
        locationQuery: String,
        historyDays: Int
    ): WeatherTrendResult = withContext(Dispatchers.IO) {
        fetchWeather(countryCode, locationQuery, historyDays)
    }

    suspend fun fetchMarketMajorSeries(days: Int): Map<String, List<CurrencyRate>> = withContext(Dispatchers.IO) {
        val range = when {
            days <= 1 -> "5d"
            days <= 7 -> "1mo"
            days <= 14 -> "3mo"
            else -> "6mo"
        }

        mapOf(
            "S&P 500" to fetchYahooSeries("^GSPC", range),
            "Dow Jones" to fetchYahooSeries("^DJI", range),
            "NASDAQ" to fetchYahooSeries("^IXIC", range)
        )
    }

    suspend fun fetchMusicItems(
        countryCode: String,
        languageCode: String,
        genreId: String
    ): List<MediaTrendItem> = withContext(Dispatchers.IO) {
        val array = fetchMusicResultsArray(countryCode, languageCode, genreId)
        val southAsian = isSouthAsianCountry(countryCode)
        val out = ArrayList<MediaTrendItem>()
        for (i in 0 until array.size()) {
            val obj = array[i].asJsonObject
            val song = firstNonBlank(
                obj.stringOrNull("trackName"),
                obj.stringOrNull("collectionName"),
                "Song ${i + 1}"
            )
            val singer = firstNonBlank(
                obj.stringOrNull("artistName"),
                "Unknown"
            )
            val movieSource = obj.stringOrNull("collectionName")
            val score = (100.0 - i).coerceAtLeast(1.0)
            val subtitle = if (southAsian && movieSource.isNotBlank()) {
                movieSource
            } else {
                singer
            }
            val subtitleLabel = if (southAsian && movieSource.isNotBlank()) subtitle else trimLabel(subtitle, 22)
            out.add(MediaTrendItem(trimLabel(song, 26), subtitleLabel, score))
        }
        out
    }

    private fun fetchMusicResultsArray(countryCode: String, languageCode: String, genreId: String): JsonArray {
        val languageKeys = movieLanguageKeywords(languageCode)
        val genreKeys = musicGenreKeywords(genreId)
        val strictLanguage = languageKeys.isNotEmpty()

        var bestLanguageOnly = JsonArray()
        var bestFallback = JsonArray()

        val preferredCountry = languagePreferredCountry(languageCode)
        val countriesToTry = listOf(preferredCountry, countryCode.lowercase())
            .filter { it.isNotBlank() }
            .distinct()

        val languageTerm = languageKeyword(languageCode)
        val genreTerm = musicGenreTerm(genreId)
        val termsToTry = listOf(
            listOf(languageTerm, genreTerm, "song").filter { it.isNotBlank() }.joinToString(" "),
            listOf(languageTerm, "song").filter { it.isNotBlank() }.joinToString(" "),
            listOf(genreTerm, "song").filter { it.isNotBlank() }.joinToString(" "),
            listOf(languageTerm, "latest", "song").filter { it.isNotBlank() }.joinToString(" "),
            "top song"
        ).filter { it.isNotBlank() }.distinct()

        for (candidateCountry in countriesToTry) {
            for (term in termsToTry) {
                val url = "https://itunes.apple.com/search" +
                    "?term=${term.replace(" ", "+")}" +
                    "&country=$candidateCountry" +
                    "&lang=${itunesLangCode(languageCode)}" +
                    "&media=music" +
                    "&entity=song" +
                    "&limit=200" +
                    "&_ts=${System.currentTimeMillis()}"

                try {
                    val results = getJson(url).getAsJsonArray("results") ?: JsonArray()
                    if (results.size() == 0) continue

                    val languageFiltered = if (strictLanguage) {
                        applyMediaKeywordFilter(results, languageKeys)
                    } else {
                        results
                    }
                    val genreBase = if (strictLanguage) languageFiltered else results
                    val genreFiltered = if (genreKeys.isNotEmpty()) {
                        applyMediaKeywordFilter(genreBase, genreKeys)
                    } else {
                        genreBase
                    }

                    if (genreFiltered.size() > 0) return genreFiltered
                    if (strictLanguage && languageFiltered.size() > bestLanguageOnly.size()) {
                        bestLanguageOnly = languageFiltered
                    }
                    if (!strictLanguage && results.size() > bestFallback.size()) {
                        bestFallback = results
                    }
                } catch (_: Exception) {
                    // Try next candidate.
                }
            }
        }

        return when {
            strictLanguage && bestLanguageOnly.size() > 0 -> bestLanguageOnly
            strictLanguage -> JsonArray()
            bestFallback.size() > 0 -> bestFallback
            else -> JsonArray()
        }
    }

    suspend fun fetchMovieItems(
        countryCode: String,
        languageCode: String,
        genreId: String
    ): List<MediaTrendItem> = withContext(Dispatchers.IO) {
        val strictLanguage = movieLanguageKeywords(languageCode).isNotEmpty()
        val strictSearchResults = if (strictLanguage) {
            fetchMovieSearchResultsArray(countryCode, languageCode, genreId)
        } else {
            JsonArray()
        }

        val strictRssResults = fetchMovieRssResultsArray(
            countryCode = countryCode,
            languageCode = languageCode,
            genreId = genreId,
            includeUsFallback = !strictLanguage
        )

        val array = when {
            strictSearchResults.size() > 0 -> strictSearchResults
            strictRssResults.size() > 0 -> strictRssResults
            !strictLanguage -> {
                val localAnyLanguageFallback = fetchMovieRssResultsArray(
                    countryCode = countryCode,
                    languageCode = "en_us",
                    genreId = genreId,
                    includeUsFallback = false
                )
                if (localAnyLanguageFallback.size() > 0) localAnyLanguageFallback
                else fetchMovieRssResultsArray(
                    countryCode = countryCode,
                    languageCode = "en_us",
                    genreId = genreId,
                    includeUsFallback = true
                )
            }
            else -> JsonArray()
        }
        val out = ArrayList<MediaTrendItem>()
        for (i in 0 until array.size()) {
            val obj = array[i].asJsonObject
            val movie = firstNonBlank(
                obj.stringOrNull("trackName"),
                obj.stringOrNull("collectionName"),
                "Movie ${i + 1}"
            )
            val artist = firstNonBlank(
                obj.stringOrNull("artistName"),
                "Unknown"
            )
            val score = (100.0 - i).coerceAtLeast(1.0)
            out.add(MediaTrendItem(trimLabel(movie, 26), trimLabel(artist, 22), score))
        }
        out
    }

    private fun fetchMovieSearchResultsArray(countryCode: String, languageCode: String, genreId: String): JsonArray {
        val languageKeys = movieLanguageKeywords(languageCode)
        if (languageKeys.isEmpty()) return JsonArray()

        val genreKeys = movieGenreKeywords(genreId)
        val preferredCountry = languagePreferredCountry(languageCode)
        val countriesToTry = listOf(preferredCountry, countryCode.lowercase())
            .filter { it.isNotBlank() }
            .distinct()

        val languageTerm = languageKeyword(languageCode).ifBlank { languageKeys.first() }
        val primaryGenreTerm = genreKeys.firstOrNull().orEmpty()
        val termsToTry = listOf(
            "$languageTerm movie",
            "$languageTerm film",
            if (primaryGenreTerm.isNotBlank()) "$languageTerm $primaryGenreTerm movie" else ""
        ).filter { it.isNotBlank() }.distinct()

        var bestLanguageOnly = JsonArray()

        for (candidateCountry in countriesToTry) {
            for (term in termsToTry) {
                val url = "https://itunes.apple.com/search" +
                    "?term=${term.replace(" ", "+")}" +
                    "&country=$candidateCountry" +
                    "&lang=${itunesLangCode(languageCode)}" +
                    "&media=movie" +
                    "&entity=movie" +
                    "&limit=100"

                try {
                    val results = getJson(url).getAsJsonArray("results") ?: JsonArray()
                    if (results.size() == 0) continue

                    val languageFiltered = applyMediaKeywordFilter(results, languageKeys)
                    if (languageFiltered.size() == 0) continue
                    val languageBase = languageFiltered
                    val genreFiltered = if (genreKeys.isNotEmpty()) {
                        applyMediaKeywordFilter(languageBase, genreKeys)
                    } else {
                        languageBase
                    }

                    if (genreFiltered.size() > 0) return genreFiltered
                    if (languageBase.size() > bestLanguageOnly.size()) {
                        bestLanguageOnly = languageBase
                    }
                } catch (_: Exception) {
                    // Try next query.
                }
            }
        }

        return bestLanguageOnly
    }

    suspend fun fetchPodcastItems(
        countryCode: String,
        languageCode: String,
        genreId: String
    ): List<MediaTrendItem> = withContext(Dispatchers.IO) {
        val array = fetchPodcastResultsArray(countryCode, languageCode, genreId)
        val out = ArrayList<MediaTrendItem>()
        for (i in 0 until array.size()) {
            val obj = array[i].asJsonObject
            val title = firstNonBlank(
                obj.stringOrNull("collectionName"),
                obj.stringOrNull("trackName"),
                "Podcast ${i + 1}"
            )
            val creator = firstNonBlank(
                obj.stringOrNull("artistName"),
                "Unknown"
            )
            val score = (100.0 - i).coerceAtLeast(1.0)
            out.add(MediaTrendItem(trimLabel(title, 26), trimLabel(creator, 22), score))
        }
        out
    }

    private fun fetchPodcastResultsArray(countryCode: String, languageCode: String, genreId: String): JsonArray {
        val strictLanguage = movieLanguageKeywords(languageCode).isNotEmpty()
        val languageKeys = movieLanguageKeywords(languageCode)

        val preferredCountry = languagePreferredCountry(languageCode)
        val countriesToTry = listOf(preferredCountry, countryCode.lowercase(), "us")
            .filter { it.isNotBlank() }
            .distinct()

        val genreSegment = if (genreId.isNotBlank() && genreId.all { it.isDigit() }) {
            "/genre=$genreId"
        } else {
            ""
        }

        var bestStrict = JsonArray()
        var bestFallback = JsonArray()

        for (candidateCountry in countriesToTry) {
            val url = "https://itunes.apple.com/$candidateCountry/rss/toppodcasts/limit=100$genreSegment/json?_ts=${System.currentTimeMillis()}"
            try {
                val json = getJson(url)
                val feed = json.getAsJsonObject("feed") ?: continue
                val entries = feed.getAsJsonArray("entry") ?: continue
                if (entries.size() == 0) continue

                val normalized = JsonArray()
                for (i in 0 until entries.size()) {
                    val src = entries[i].asJsonObject
                    val obj = JsonObject()
                    val name = src.getAsJsonObject("im:name")?.stringOrNull("label").orEmpty()
                    val artist = src.getAsJsonObject("im:artist")?.stringOrNull("label").orEmpty()
                    val summary = src.getAsJsonObject("summary")?.stringOrNull("label").orEmpty()
                    obj.addProperty("collectionName", name)
                    obj.addProperty("trackName", name)
                    obj.addProperty("artistName", artist)
                    obj.addProperty("summary", summary)
                    obj.addProperty("_source", "rss_top")
                    normalized.add(obj)
                }

                if (strictLanguage) {
                    val filtered = applyMediaKeywordFilter(normalized, languageKeys)
                    if (filtered.size() > 0) return filtered
                    if (filtered.size() > bestStrict.size()) bestStrict = filtered
                }

                if (normalized.size() > bestFallback.size()) bestFallback = normalized
            } catch (_: Exception) {
                // Try next country fallback.
            }
        }

        if (strictLanguage && bestStrict.size() > 0) return bestStrict
        if (bestFallback.size() > 0) return bestFallback

        return fetchMediaResultsArray("podcast", countryCode, languageCode, genreId)
    }

    private fun fetchWeather(countryCode: String, locationQuery: String, days: Int): WeatherTrendResult {
        val (lat, lon) = resolveLocation(countryCode, locationQuery)
        val historyCount = days.coerceIn(1, 30)
        val today = java.time.LocalDate.now()
        val start = today.minusDays((historyCount - 1).toLong()).toString()
        val end = today.toString()

        val archiveUrl = "https://archive-api.open-meteo.com/v1/archive" +
            "?latitude=$lat&longitude=$lon&start_date=$start&end_date=$end&daily=temperature_2m_mean&timezone=auto"

        val forecastUrl = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=$lat&longitude=$lon&daily=temperature_2m_mean&forecast_days=7&timezone=auto"

        val historical = parseDailySeries(getJson(archiveUrl))
        val forecast = parseDailySeries(getJson(forecastUrl))
        return WeatherTrendResult(historical = historical, forecast = forecast)
    }

    private fun parseDailySeries(json: JsonObject): List<CurrencyRate> {
        val daily = json.getAsJsonObject("daily") ?: return emptyList()
        val times = daily.getAsJsonArray("time") ?: return emptyList()
        val vals = daily.getAsJsonArray("temperature_2m_mean") ?: return emptyList()

        val out = ArrayList<CurrencyRate>()
        val count = minOf(times.size(), vals.size())
        for (i in 0 until count) {
            val v = vals[i]
            if (v == null || v.isJsonNull) continue
            out.add(CurrencyRate(times[i].asString, v.asDouble))
        }
        return out
    }

    private fun fetchMarket(days: Int, selectedIndex: String, countryCode: String): List<CurrencyRate> {
        val range = when {
            days <= 1 -> "5d"
            days <= 7 -> "1mo"
            days <= 14 -> "3mo"
            else -> "6mo"
        }

        val points = if (selectedIndex == "ALL_MAJOR") {
            val sp = fetchYahooSeries("^GSPC", range)
            val dj = fetchYahooSeries("^DJI", range)
            val nq = fetchYahooSeries("^IXIC", range)
            combineSeriesAverage(listOf(sp, dj, nq))
        } else {
            fetchMarketWithFallback(selectedIndex, countryCode, range)
        }

        return if (days <= 1) points.takeLast(1) else points.takeLast(days)
    }

    private fun fetchMarketWithFallback(selectedIndex: String, countryCode: String, range: String): List<CurrencyRate> {
        val candidates = mutableListOf<String>()
        if (selectedIndex.isNotBlank()) {
            candidates.add(selectedIndex)
        }
        TrendOptions.marketSymbolsForCountry(countryCode).forEach { code ->
            if (code.isNotBlank() && code != "ALL_MAJOR" && candidates.none { it == code }) {
                candidates.add(code)
            }
        }
        if (candidates.isEmpty()) {
            candidates.add("^GSPC")
        }

        for (symbol in candidates) {
            val series = fetchYahooSeries(symbol, range)
            if (series.isNotEmpty()) {
                return series
            }
        }

        return emptyList()
    }

    private fun fetchYahooSeries(symbol: String, range: String): List<CurrencyRate> {
        val encoded = java.net.URLEncoder.encode(symbol, Charsets.UTF_8.name())
        val url = "https://query1.finance.yahoo.com/v8/finance/chart/$encoded?interval=1d&range=$range"
        val json = getJson(url)

        val result = json.getAsJsonObject("chart")
            .getAsJsonArray("result")[0]
            .asJsonObject
        val timestamps = result.getAsJsonArray("timestamp")
        val closes = result.getAsJsonObject("indicators")
            .getAsJsonArray("quote")[0]
            .asJsonObject
            .getAsJsonArray("close")

        val out = ArrayList<CurrencyRate>()
        val count = minOf(timestamps.size(), closes.size())
        for (i in 0 until count) {
            val closeEl = closes[i]
            if (closeEl == null || closeEl.isJsonNull) continue
            val ts = timestamps[i].asLong
            val date = java.time.Instant.ofEpochSecond(ts)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .toString()
            out.add(CurrencyRate(date, closeEl.asDouble))
        }
        return out
    }

    private fun combineSeriesAverage(seriesList: List<List<CurrencyRate>>): List<CurrencyRate> {
        val grouped = linkedMapOf<String, MutableList<Double>>()
        for (series in seriesList) {
            for (item in series) {
                grouped.getOrPut(item.date) { mutableListOf() }.add(item.rate)
            }
        }

        return grouped.entries
            .mapNotNull { (date, values) ->
                if (values.isEmpty()) null else CurrencyRate(date, values.average())
            }
            .sortedBy { it.date }
    }

    private fun fetchMedia(
        media: String,
        countryCode: String,
        languageCode: String,
        genreId: String
    ): List<CurrencyRate> {
        val results = fetchMediaResultsArray(media, countryCode, languageCode, genreId)
        if (results.size() == 0) return emptyList()

        return mapMediaResults(results)
    }

    private fun fetchMediaResultsArray(
        media: String,
        countryCode: String,
        languageCode: String,
        genreId: String
    ): JsonArray {
        val baseTerm = when (media) {
            "musicTrack" -> "top song"
            "movie" -> "top movie"
            else -> "top podcast"
        }

        val keyword = languageKeyword(languageCode)
        val term = if (keyword.isBlank()) baseTerm else "$keyword $baseTerm"

        val entity = when (media) {
            "musicTrack" -> "song"
            "movie" -> "movie"
            else -> "podcast"
        }

        val url = "https://itunes.apple.com/search" +
            "?term=${term.replace(" ", "+")}" +
            "&country=$countryCode" +
            "&lang=${itunesLangCode(languageCode)}" +
            "&media=${media.removeSuffix("Track")}" +
            "&entity=$entity" +
            "&genreId=$genreId" +
            "&limit=100" +
            "&_ts=${System.currentTimeMillis()}"

        val json = getJson(url)
        val results = json.getAsJsonArray("results") ?: JsonArray()
        if (media == "podcast") {
            for (i in 0 until results.size()) {
                val item = results[i].asJsonObject
                item.addProperty("_source", "itunes_search")
            }
        }
        return results
    }

    private fun fetchMovieRssResultsArray(
        countryCode: String,
        languageCode: String,
        genreId: String,
        includeUsFallback: Boolean = true
    ): JsonArray {
        val languageKeys = movieLanguageKeywords(languageCode)
        val genreKeys = movieGenreKeywords(genreId)
        val strictLanguage = languageKeys.isNotEmpty()

        val languageCountry = languagePreferredCountry(languageCode)
        val countriesToTry = listOf(
            languageCountry,
            countryCode.lowercase(),
            if (!strictLanguage && includeUsFallback) "us" else ""
        )
            .filter { it.isNotBlank() }
            .distinct()

        val genreSegment = if (genreId.isNotBlank() && genreId.all { it.isDigit() }) {
            "/genre=$genreId"
        } else {
            ""
        }

        var bestLanguageOnly = JsonArray()
        var bestGenreOnly = JsonArray()
        var bestFallback = JsonArray()
        var bestStrictLocalFallback = JsonArray()

        for (candidateCountry in countriesToTry) {
            val url = "https://itunes.apple.com/$candidateCountry/rss/topmovies$genreSegment/limit=100/json"
            try {
                val json = getJson(url)
                val feed = json.getAsJsonObject("feed") ?: continue
                val entries = feed.getAsJsonArray("entry") ?: continue
                if (entries.size() == 0) continue

                val normalized = JsonArray()
                for (i in 0 until entries.size()) {
                    val src = entries[i].asJsonObject
                    val obj = JsonObject()
                    val name = src.getAsJsonObject("im:name")?.stringOrNull("label").orEmpty()
                    val artist = src.getAsJsonObject("im:artist")?.stringOrNull("label").orEmpty()
                    val summary = src.getAsJsonObject("summary")?.stringOrNull("label").orEmpty()
                    obj.addProperty("trackName", name)
                    obj.addProperty("artistName", artist)
                    obj.addProperty("collectionName", name)
                    obj.addProperty("summary", summary)
                    normalized.add(obj)
                }
                val languageFiltered = if (strictLanguage) {
                    applyMediaKeywordFilter(normalized, languageKeys)
                } else {
                    normalized
                }
                val baseForGenre = if (strictLanguage) languageFiltered else normalized
                val genreFiltered = if (genreKeys.isNotEmpty()) {
                    applyMediaKeywordFilter(baseForGenre, genreKeys)
                } else {
                    baseForGenre
                }

                if (genreFiltered.size() > 0) return genreFiltered
                if (strictLanguage && languageFiltered.size() > bestLanguageOnly.size()) {
                    bestLanguageOnly = languageFiltered
                }
                if (strictLanguage && (candidateCountry == countryCode.lowercase() || candidateCountry == languageCountry) && normalized.size() > bestStrictLocalFallback.size()) {
                    bestStrictLocalFallback = normalized
                }
                if (!strictLanguage && genreKeys.isNotEmpty() && genreFiltered.size() > bestGenreOnly.size()) {
                    bestGenreOnly = genreFiltered
                }
                if (!strictLanguage && normalized.size() > bestFallback.size()) {
                    bestFallback = normalized
                }
            } catch (_: Exception) {
                // Try next country fallback.
            }
        }

        return when {
            strictLanguage && bestLanguageOnly.size() > 0 -> bestLanguageOnly
            strictLanguage && bestStrictLocalFallback.size() > 0 -> bestStrictLocalFallback
            strictLanguage -> JsonArray()
            bestGenreOnly.size() > 0 -> bestGenreOnly
            bestFallback.size() > 0 -> bestFallback
            else -> JsonArray()
        }
    }

    private fun applyMovieLanguageFilter(items: JsonArray, languageCode: String): JsonArray {
        val keywords = movieLanguageKeywords(languageCode)
        return applyMediaKeywordFilter(items, keywords)
    }

    private fun applyMovieGenreFilter(items: JsonArray, genreId: String): JsonArray {
        val keywords = movieGenreKeywords(genreId)
        return applyMediaKeywordFilter(items, keywords)
    }

    private fun applyMediaKeywordFilter(items: JsonArray, keywords: List<String>): JsonArray {
        if (keywords.isEmpty()) return items

        val filtered = JsonArray()
        for (i in 0 until items.size()) {
            val obj = items[i].asJsonObject
            val haystack = (
                obj.stringOrNull("trackName") + " " +
                obj.stringOrNull("collectionName") + " " +
                obj.stringOrNull("artistName") + " " +
                obj.stringOrNull("summary")
            ).lowercase()

            if (keywords.any { haystack.contains(it) }) {
                filtered.add(obj)
            }
        }
        return filtered
    }

    private fun movieLanguageKeywords(languageCode: String): List<String> = when (languageCode.lowercase()) {
        "ar_sa", "ar_ae" -> listOf("arabic", "arab", "saudi", "emirati")
        "zh_cn" -> listOf("chinese", "mandarin", "china")
        "ms_my" -> listOf("malay", "malaysia")
        "th_th" -> listOf("thai", "thailand")
        "id_id" -> listOf("indonesian", "indonesia")
        "tl_ph" -> listOf("filipino", "tagalog", "philippines")
        "ur_pk" -> listOf("urdu", "pakistan", "lollywood")
        "si_lk" -> listOf("sinhala", "sri lanka")
        "ne_np" -> listOf("nepali", "nepal")
        "no_no" -> listOf("norwegian", "norsk")
        "da_dk" -> listOf("danish", "dansk")
        "hi_in" -> listOf("hindi", "bollywood")
        "ta_in" -> listOf("tamil", "kollywood")
        "te_in" -> listOf("telugu", "tollywood")
        "ml_in" -> listOf("malayalam", "mollywood")
        "kn_in" -> listOf("kannada", "sandalwood")
        "mr_in" -> listOf("marathi")
        "bn_in" -> listOf("bengali", "bangla")
        "ja_jp" -> listOf("japanese", "japan")
        "ko_kr" -> listOf("korean", "korea")
        "fr_fr" -> listOf("french", "france")
        "de_de" -> listOf("german", "deutsch")
        "es_es" -> listOf("spanish", "espanol")
        "it_it" -> listOf("italian", "italia")
        "pt_br" -> listOf("portuguese", "brasil", "brazil")
        else -> emptyList()
    }

    private fun movieGenreKeywords(genreId: String): List<String> = when (genreId) {
        "4401" -> listOf("action", "adventure", "fight", "war", "hero")
        "4404" -> listOf("comedy", "funny", "humor", "satire", "rom-com")
        "4406" -> listOf("drama", "family", "emotional", "biopic")
        "4413" -> listOf("sci-fi", "science fiction", "fantasy", "space", "future")
        "4416" -> listOf("thriller", "crime", "mystery", "suspense", "detective")
        else -> emptyList()
    }

    private fun musicGenreKeywords(genreId: String): List<String> = when (genreId) {
        "14" -> listOf("pop")
        "18" -> listOf("hip hop", "rap")
        "21" -> listOf("rock")
        "11" -> listOf("jazz")
        "5" -> listOf("classical")
        "love" -> listOf("love", "romantic")
        "inspirational" -> listOf("inspirational", "motivation", "uplifting")
        "fast_beat" -> listOf("dance", "party", "fast beat", "energetic")
        "devotional" -> listOf("devotional", "bhajan", "spiritual")
        else -> emptyList()
    }

    private fun musicGenreTerm(genreId: String): String = when (genreId) {
        "14" -> "pop"
        "18" -> "hip hop"
        "21" -> "rock"
        "11" -> "jazz"
        "5" -> "classical"
        "love" -> "love"
        "inspirational" -> "inspirational"
        "fast_beat" -> "dance"
        "devotional" -> "devotional"
        else -> ""
    }

    private fun languagePreferredCountry(languageCode: String): String = when (languageCode.lowercase()) {
        "hi_in", "ta_in", "te_in", "ml_in", "kn_in", "mr_in", "bn_in", "en_in" -> "in"
        "ur_pk" -> "pk"
        "si_lk" -> "lk"
        "ne_np" -> "np"
        "ar_sa" -> "sa"
        "ar_ae" -> "ae"
        "zh_cn" -> "cn"
        "ms_my" -> "my"
        "th_th" -> "th"
        "id_id" -> "id"
        "tl_ph" -> "ph"
        "no_no" -> "no"
        "da_dk" -> "dk"
        "fr_ca", "en_ca" -> "ca"
        "es_mx" -> "mx"
        "en_au" -> "au"
        "en_nz" -> "nz"
        "ja_jp" -> "jp"
        "ko_kr" -> "kr"
        "fr_fr" -> "fr"
        "de_de" -> "de"
        "es_es" -> "es"
        "it_it" -> "it"
        "pt_br" -> "br"
        "en_gb" -> "gb"
        else -> ""
    }

    private fun mapMediaResults(results: JsonArray): List<CurrencyRate> {
        val out = ArrayList<CurrencyRate>()
        for (i in 0 until results.size()) {
            val obj = results[i].asJsonObject
            val title = firstNonBlank(
                obj.stringOrNull("trackName"),
                obj.stringOrNull("collectionName"),
                obj.stringOrNull("artistName"),
                "Item ${i + 1}"
            )
            // Rank-based score from live top-search snapshot (higher is better rank).
            val score = (100.0 - i).coerceAtLeast(1.0)
            out.add(CurrencyRate(trimLabel(title, 24), score))
        }
        return out
    }

    private fun getJson(url: String): JsonObject {
        val txt = getText(url)
        return JsonParser().parse(txt).asJsonObject
    }

    private fun resolveLocation(countryCode: String, query: String): Pair<Double, Double> {
        if (query.isBlank()) return countryToLatLon(countryCode)

        val encoded = URLEncoder.encode(query, Charsets.UTF_8.name())
        val url = "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=en&countryCode=${countryCode.uppercase()}"

        return try {
            val json = getJson(url)
            val results = json.getAsJsonArray("results")
            if (results != null && results.size() > 0) {
                val first = results[0].asJsonObject
                first.get("latitude").asDouble to first.get("longitude").asDouble
            } else {
                countryToLatLon(countryCode)
            }
        } catch (_: Exception) {
            countryToLatLon(countryCode)
        }
    }

    private fun getText(url: String): String {
        val req = Request.Builder()
            .url(url)
            .header("Cache-Control", "no-cache")
            .header("Pragma", "no-cache")
            .build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw IOException("HTTP ${resp.code}: ${resp.message}")
            }
            return resp.body?.string().orEmpty()
        }
    }

    private fun countryToLatLon(countryCode: String): Pair<Double, Double> = when (countryCode.lowercase()) {
        "in" -> 28.61 to 77.21
        "gb" -> 51.50 to -0.12
        "ca" -> 45.42 to -75.69
        "au" -> -33.87 to 151.21
        "de" -> 52.52 to 13.40
        "fr" -> 48.85 to 2.35
        "jp" -> 35.68 to 139.76
        else -> 40.71 to -74.01
    }

    private fun trimLabel(s: String, max: Int): String =
        if (s.length <= max) s else s.take(max - 1) + "…"

    private fun itunesLangCode(languageCode: String): String = when (languageCode.lowercase()) {
        "en_gb", "en_in", "en_au", "en_nz", "en_ca" -> "en_gb"
        "fr_fr" -> "fr_fr"
        "fr_ca" -> "fr_fr"
        "de_de" -> "de_de"
        "es_es", "es_mx" -> "es_es"
        "ja_jp" -> "ja_jp"
        "it_it" -> "it_it"
        "pt_br" -> "pt_br"
        "ko_kr" -> "ko_kr"
        "zh_cn" -> "zh_cn"
        "ar_sa", "ar_ae" -> "ar_sa"
        "ms_my" -> "ms_my"
        "th_th" -> "th_th"
        "id_id" -> "id_id"
        "tl_ph" -> "tl_ph"
        "ur_pk" -> "ur_pk"
        "si_lk" -> "si_lk"
        "ne_np" -> "ne_np"
        "no_no" -> "no_no"
        "da_dk" -> "da_dk"
        else -> "en_us"
    }

    private fun languageKeyword(languageCode: String): String = when (languageCode.lowercase()) {
        "ar_sa", "ar_ae" -> "arabic"
        "zh_cn" -> "chinese"
        "ms_my" -> "malay"
        "th_th" -> "thai"
        "id_id" -> "indonesian"
        "tl_ph" -> "filipino"
        "ur_pk" -> "urdu"
        "si_lk" -> "sinhala"
        "ne_np" -> "nepali"
        "no_no" -> "norwegian"
        "da_dk" -> "danish"
        "hi_in" -> "hindi"
        "ta_in" -> "tamil"
        "te_in" -> "telugu"
        "ml_in" -> "malayalam"
        "kn_in" -> "kannada"
        "mr_in" -> "marathi"
        "bn_in" -> "bengali"
        "fr_ca" -> "french"
        "en_ca" -> "english"
        "es_mx" -> "spanish"
        "ja_jp" -> "japanese"
        "fr_fr" -> "french"
        "de_de" -> "german"
        "es_es" -> "spanish"
        "it_it" -> "italian"
        "pt_br" -> "portuguese"
        "ko_kr" -> "korean"
        else -> ""
    }

    private fun isSouthAsianCountry(countryCode: String): Boolean = when (countryCode.lowercase()) {
        "in", "pk", "bd", "lk", "np", "bt", "mv" -> true
        else -> false
    }

    private fun firstNonBlank(vararg values: String): String =
        values.firstOrNull { it.isNotBlank() } ?: "Item"

    private fun JsonObject.stringOrNull(key: String): String {
        if (!this.has(key)) return ""
        return try {
            this.get(key).asString.orEmpty()
        } catch (_: Exception) {
            ""
        }
    }
}
