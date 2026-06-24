package com.trendpulse.app

import com.trendpulse.app.api.RetrofitClient
import com.trendpulse.app.models.CurrencyOption
import com.trendpulse.app.models.CurrencyRate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CurrencyRepository {

    private val api = RetrofitClient.api
    private val fmt = DateTimeFormatter.ISO_DATE

    suspend fun getHistoricalRates(base: String, target: String, days: Int): List<CurrencyRate> {
        val endDate   = LocalDate.now().format(fmt)
        val startDate = LocalDate.now().minusDays((days - 1).coerceAtLeast(0).toLong()).format(fmt)
        val response  = api.getHistoricalRates(startDate, endDate, base, target)
        return response.rates
            .mapNotNull { (date, rateMap) ->
                rateMap[target]?.let { CurrencyRate(date, it) }
            }
            .sortedBy { it.date }
    }

    suspend fun getCurrencies(): List<CurrencyOption> =
        api.getCurrencies()
            .map { (code, name) -> CurrencyOption(code, name) }
            .sortedBy { it.code }
}
