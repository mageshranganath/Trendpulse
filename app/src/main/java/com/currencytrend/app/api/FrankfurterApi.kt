package com.trendpulse.app.api

import com.trendpulse.app.models.FrankfurterHistoricalResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FrankfurterApi {

    /**
     * Historical rates over a date range.
     * e.g. GET /2024-01-01..2024-01-31?base=CAD&symbols=INR
     */
    @GET("{startDate}..{endDate}")
    suspend fun getHistoricalRates(
        @Path("startDate") startDate: String,
        @Path("endDate")   endDate: String,
        @Query("base")     base: String,
        @Query("symbols")  symbols: String
    ): FrankfurterHistoricalResponse

    /** Returns map of { "USD": "United States Dollar", ... } */
    @GET("currencies")
    suspend fun getCurrencies(): Map<String, String>
}
