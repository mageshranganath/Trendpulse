package com.trendpulse.app.models

/** Response from /YYYY-MM-DD..YYYY-MM-DD?base=X&symbols=Y */
data class FrankfurterHistoricalResponse(
    val amount: Double,
    val base: String,
    val start_date: String,
    val end_date: String,
    /** outer key = date string, inner key = currency code */
    val rates: Map<String, Map<String, Double>>
)

/** One data point for the chart and table */
data class CurrencyRate(
    val date: String,
    val rate: Double
)

/** Item shown in the currency selector spinners */
data class CurrencyOption(
    val code: String,
    val name: String
) {
    override fun toString() = "$code  –  $name"
}
