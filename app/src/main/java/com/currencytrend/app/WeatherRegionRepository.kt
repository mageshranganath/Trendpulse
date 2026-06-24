package com.trendpulse.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser

class WeatherRegionRepository {

    private val client = OkHttpClient.Builder().build()
    private var countriesCache: List<CountryRegionEntry>? = null
    private val citiesCache = mutableMapOf<String, List<OptionItem>>()

    suspend fun getCountries(): List<OptionItem> = withContext(Dispatchers.IO) {
        loadCountryEntries().map { OptionItem(it.iso2.lowercase(), it.name) }
            .sortedBy { it.label }
    }

    suspend fun getStates(countryCode: String, countryName: String): List<OptionItem> = withContext(Dispatchers.IO) {
        val entry = loadCountryEntries().firstOrNull {
            it.iso2.equals(countryCode, ignoreCase = true) || it.name.equals(countryName, ignoreCase = true)
        }

        val states = entry?.states.orEmpty().sortedBy { it.label }
        if (states.isNotEmpty()) states else fallbackStates(countryCode)
    }

    suspend fun getCities(countryCode: String, countryName: String, stateName: String): List<OptionItem> = withContext(Dispatchers.IO) {
        val cacheKey = "${countryCode.lowercase()}|${stateName.lowercase()}"
        citiesCache[cacheKey]?.let { return@withContext it }

        val resolvedCountry = if (countryName.isNotBlank()) countryName else {
            loadCountryEntries().firstOrNull { it.iso2.equals(countryCode, ignoreCase = true) }?.name ?: countryCode
        }

        val payload = JsonObject().apply {
            addProperty("country", resolvedCountry)
            addProperty("state", stateName)
        }.toString()

        val request = Request.Builder()
            .url("https://countriesnow.space/api/v0.1/countries/state/cities")
            .post(payload.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .header("Cache-Control", "no-cache")
            .header("Pragma", "no-cache")
            .build()

        val cities = try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use emptyList()
                val json = JsonParser().parse(response.body?.string().orEmpty()).asJsonObject
                parseStringList(json.getAsJsonArray("data"))
            }
        } catch (_: Exception) {
            emptyList()
        }.ifEmpty { fallbackCities(countryCode, stateName) }

        citiesCache[cacheKey] = cities
        cities
    }

    private fun loadCountryEntries(): List<CountryRegionEntry> {
        countriesCache?.let { return it }

        val request = Request.Builder()
            .url("https://countriesnow.space/api/v0.1/countries/states")
            .header("Cache-Control", "no-cache")
            .header("Pragma", "no-cache")
            .build()

        val entries = try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use emptyList()
                val json = JsonParser().parse(response.body?.string().orEmpty()).asJsonObject
                val data = json.getAsJsonArray("data") ?: return@use emptyList()
                parseCountryEntries(data)
            }
        } catch (_: Exception) {
            emptyList()
        }

        countriesCache = entries.ifEmpty { fallbackCountryEntries() }
        return countriesCache.orEmpty()
    }

    private fun parseCountryEntries(data: JsonArray): List<CountryRegionEntry> {
        val entries = ArrayList<CountryRegionEntry>()
        for (i in 0 until data.size()) {
            val country = data[i].asJsonObject
            val name = country.stringOrNull("name")
            val iso2 = country.stringOrNull("iso2")
            if (name.isBlank() || iso2.isBlank()) continue

            val statesArray = country.getAsJsonArray("states") ?: JsonArray()
            val states = ArrayList<OptionItem>()
            for (j in 0 until statesArray.size()) {
                val state = statesArray[j].asJsonObject
                val stateName = state.stringOrNull("name")
                if (stateName.isNotBlank()) {
                    states.add(OptionItem(stateName, stateName))
                }
            }
            states.sortBy { it.label }
            entries.add(CountryRegionEntry(name = name, iso2 = iso2, states = states))
        }
        return entries.sortedBy { it.name }
    }

    private fun parseStringList(array: JsonArray?): List<OptionItem> {
        if (array == null) return emptyList()
        val out = ArrayList<OptionItem>()
        for (i in 0 until array.size()) {
            val value = array[i].asString.orEmpty()
            if (value.isNotBlank()) {
                out.add(OptionItem(value, value))
            }
        }
        return out.sortedBy { it.label }
    }

    private fun fallbackStates(countryCode: String): List<OptionItem> {
        val states = TrendOptions.statesForCountry(countryCode)
        return if (states.isNotEmpty()) states.sortedBy { it.label } else listOf(OptionItem("default", "Default Region"))
    }

    private fun fallbackCities(countryCode: String, stateName: String): List<OptionItem> {
        val cities = TrendOptions.citiesForState(countryCode, stateName)
        return if (cities.isNotEmpty()) cities.sortedBy { it.label } else listOf(OptionItem("", "Default City"))
    }

    private fun fallbackCountryEntries(): List<CountryRegionEntry> {
        return TrendOptions.countries.map { CountryRegionEntry(name = it.label, iso2 = it.code, states = emptyList()) }
    }

    private data class CountryRegionEntry(
        val name: String,
        val iso2: String,
        val states: List<OptionItem>
    )

    private fun JsonObject.stringOrNull(key: String): String {
        if (!has(key)) return ""
        return try {
            get(key).asString.orEmpty()
        } catch (_: Exception) {
            ""
        }
    }
}