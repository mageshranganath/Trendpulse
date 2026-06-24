package com.trendpulse.app

import android.graphics.Color
import android.os.Bundle
import android.content.res.Configuration
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.trendpulse.app.databinding.ActivityTrendDetailBinding
import com.trendpulse.app.models.CurrencyRate
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import kotlin.math.abs

class TrendDetailActivity : BaseTrendActivity() {

    companion object {
        const val EXTRA_TREND_TYPE = "extra_trend_type"
    }

    private lateinit var binding: ActivityTrendDetailBinding
    private val rateAdapter = RateAdapter()
    private val mediaAdapter = MediaTrendAdapter()
    private val repo = LiveTrendRepository()
    private val regionRepo = WeatherRegionRepository()

    private lateinit var trendType: TrendType

    private lateinit var periodAdapter: ArrayAdapter<OptionItem>
    private lateinit var countryAdapter: ArrayAdapter<OptionItem>
    private lateinit var languageAdapter: ArrayAdapter<OptionItem>
    private lateinit var genreAdapter: ArrayAdapter<OptionItem>
    private lateinit var stateAdapter: ArrayAdapter<OptionItem>
    private lateinit var cityAdapter: ArrayAdapter<OptionItem>

    private val forecastAdapter = RateAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrendDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trendType = TrendType.fromName(intent.getStringExtra(EXTRA_TREND_TYPE))

        setupToolbarBack(binding.toolbarTrend)
        supportActionBar?.title = trendType.label

        setupList()
        setupChart()
        setupFilters()
        setupFetchButton()
        loadCountries()
        setBlankState()
    }

    private fun setupList() {
        binding.recyclerTrend.layoutManager = LinearLayoutManager(this)
        binding.recyclerTrend.adapter = rateAdapter
        binding.recyclerTrend.isNestedScrollingEnabled = true

        binding.recyclerForecastTrend.layoutManager = LinearLayoutManager(this)
        binding.recyclerForecastTrend.adapter = forecastAdapter
        binding.recyclerForecastTrend.isNestedScrollingEnabled = true
    }

    private fun setupFilters() {
        periodAdapter = makeAdapter(TrendOptions.periods)
        countryAdapter = makeAdapter(TrendOptions.countries)
        languageAdapter = makeAdapter(TrendOptions.languages)

        binding.spinnerPeriodTrend.adapter = periodAdapter
        binding.spinnerCountryTrend.adapter = countryAdapter
        binding.spinnerLanguageTrend.adapter = languageAdapter

        stateAdapter = makeAdapter(emptyList())
        cityAdapter = makeAdapter(emptyList())
        binding.spinnerStateTrend.adapter = stateAdapter
        binding.spinnerCityTrend.adapter = cityAdapter

        binding.spinnerPeriodTrend.setSelection(0) // Today by default
        binding.spinnerCountryTrend.setSelection(0)
        updateLanguagesForCountry("us")

        when (trendType) {
            TrendType.MARKET -> {
                binding.layoutLanguage.visibility = View.GONE
                binding.layoutGenre.visibility = View.VISIBLE
                binding.layoutLocationInput.visibility = View.GONE
                binding.textGenreLabel.setText(R.string.label_index)
                genreAdapter = makeAdapter(TrendOptions.marketIndexesForCountry((binding.spinnerCountryTrend.selectedItem as? OptionItem)?.code ?: "us"))
                binding.spinnerGenreTrend.adapter = genreAdapter
                binding.spinnerGenreTrend.setSelection(0)
                binding.textColLeft.setText(R.string.col_date)
                binding.textColRight.setText(R.string.trend_col_value)
                binding.cardChartTrend.visibility = View.VISIBLE
                binding.cardForecastTrend.visibility = View.GONE
                binding.recyclerTrend.adapter = rateAdapter
            }
            TrendType.MUSIC -> {
                binding.layoutLanguage.visibility = View.VISIBLE
                binding.layoutGenre.visibility = View.VISIBLE
                binding.layoutLocationInput.visibility = View.GONE
                binding.layoutWeatherState.visibility = View.GONE
                binding.layoutWeatherCity.visibility = View.GONE
                binding.textGenreLabel.setText(R.string.label_genre)
                genreAdapter = makeAdapter(TrendOptions.musicGenresForCountry((binding.spinnerCountryTrend.selectedItem as? OptionItem)?.code ?: "us"))
                binding.spinnerGenreTrend.adapter = genreAdapter
                binding.textColLeft.setText(R.string.trend_col_song)
                updateMusicColumnHeaders((binding.spinnerCountryTrend.selectedItem as? OptionItem)?.code ?: "us")
                binding.cardChartTrend.visibility = View.GONE
                binding.cardForecastTrend.visibility = View.GONE
                binding.layoutTrendStats.visibility = View.GONE
                binding.recyclerTrend.adapter = mediaAdapter
            }
            TrendType.MOVIES -> {
                binding.layoutLanguage.visibility = View.VISIBLE
                binding.layoutGenre.visibility = View.VISIBLE
                binding.layoutLocationInput.visibility = View.GONE
                binding.layoutWeatherState.visibility = View.GONE
                binding.layoutWeatherCity.visibility = View.GONE
                binding.textGenreLabel.setText(R.string.label_genre)
                genreAdapter = makeAdapter(TrendOptions.movieGenres)
                binding.spinnerGenreTrend.adapter = genreAdapter
                binding.textColLeft.setText(R.string.trend_col_movie)
                binding.textColRight.setText(R.string.trend_col_artist)
                binding.cardChartTrend.visibility = View.GONE
                binding.cardForecastTrend.visibility = View.GONE
                binding.layoutTrendStats.visibility = View.GONE
                binding.recyclerTrend.adapter = mediaAdapter
            }
            TrendType.PODCAST -> {
                binding.layoutLanguage.visibility = View.VISIBLE
                binding.layoutGenre.visibility = View.VISIBLE
                binding.layoutLocationInput.visibility = View.GONE
                binding.layoutWeatherState.visibility = View.GONE
                binding.layoutWeatherCity.visibility = View.GONE
                binding.textGenreLabel.setText(R.string.label_genre)
                genreAdapter = makeAdapter(TrendOptions.podcastGenres)
                binding.spinnerGenreTrend.adapter = genreAdapter
                binding.textColLeft.setText(R.string.trend_col_podcast)
                binding.textColRight.setText(R.string.trend_col_creator)
                binding.cardChartTrend.visibility = View.GONE
                binding.cardForecastTrend.visibility = View.GONE
                binding.layoutTrendStats.visibility = View.GONE
                binding.recyclerTrend.adapter = mediaAdapter
            }
            TrendType.WEATHER -> {
                binding.layoutLanguage.visibility = View.GONE
                binding.layoutGenre.visibility = View.GONE
                binding.layoutLocationInput.visibility = View.GONE
                binding.layoutWeatherState.visibility = View.VISIBLE
                binding.layoutWeatherCity.visibility = View.VISIBLE
                genreAdapter = makeAdapter(emptyList())
                binding.spinnerGenreTrend.adapter = genreAdapter
                // Always show next-week forecast for weather.
                val weekIndex = TrendOptions.periods.indexOfFirst { it.code == "7" }
                if (weekIndex >= 0) binding.spinnerPeriodTrend.setSelection(weekIndex)
                binding.textColLeft.setText(R.string.col_date)
                binding.textColRight.setText(R.string.trend_col_value)
                binding.cardChartTrend.visibility = View.VISIBLE
                binding.cardForecastTrend.visibility = View.VISIBLE
                binding.layoutTrendStats.visibility = View.VISIBLE
                binding.recyclerTrend.adapter = rateAdapter

                updateWeatherStatesAndCities(
                    (binding.spinnerCountryTrend.selectedItem as? OptionItem)?.code ?: "us",
                    (binding.spinnerCountryTrend.selectedItem as? OptionItem)?.label ?: "United States"
                )
                binding.spinnerCountryTrend.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selected = binding.spinnerCountryTrend.selectedItem as? OptionItem
                        val code = selected?.code ?: "us"
                        val label = selected?.label ?: "United States"
                        updateWeatherStatesAndCities(code, label)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                }

                binding.spinnerStateTrend.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selected = binding.spinnerCountryTrend.selectedItem as? OptionItem
                        val countryCode = selected?.code ?: "us"
                        val countryLabel = selected?.label ?: "United States"
                        val state = (binding.spinnerStateTrend.selectedItem as? OptionItem)?.code.orEmpty()
                        updateWeatherCities(countryCode, countryLabel, state)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                }
            }
        }

        if (trendType == TrendType.MUSIC || trendType == TrendType.MOVIES || trendType == TrendType.PODCAST) {
            binding.spinnerCountryTrend.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val code = (binding.spinnerCountryTrend.selectedItem as? OptionItem)?.code ?: "us"
                    updateLanguagesForCountry(code)
                    if (trendType == TrendType.MUSIC) {
                        updateMusicGenresForCountry(code)
                        updateMusicColumnHeaders(code)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }

        if (trendType == TrendType.MARKET) {
            binding.spinnerCountryTrend.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val code = (binding.spinnerCountryTrend.selectedItem as? OptionItem)?.code ?: "us"
                    updateMarketIndexesForCountry(code)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }
    }

    private fun setupFetchButton() {
        binding.buttonFetchTrend.setOnClickListener {
            fetchLiveTrend()
        }
    }

    private fun fetchLiveTrend() {
        val days = selectedPeriodDays()
        val country = selectedCode(binding.spinnerCountryTrend.selectedItem as? OptionItem, "us")
        val language = selectedCode(binding.spinnerLanguageTrend.selectedItem as? OptionItem, "en_us")
        val genre = selectedCode(binding.spinnerGenreTrend.selectedItem as? OptionItem, "all")
        val location = when (trendType) {
            TrendType.WEATHER -> {
                val city = (binding.spinnerCityTrend.selectedItem as? OptionItem)?.code.orEmpty()
                val state = (binding.spinnerStateTrend.selectedItem as? OptionItem)?.code.orEmpty()
                listOf(city, state).filter { it.isNotBlank() }.joinToString(", ")
            }
            else -> binding.editWeatherLocation.text?.toString()?.trim().orEmpty()
        }

        lifecycleScope.launch {
            setLoading(true)
            try {
                when (trendType) {
                    TrendType.MUSIC -> {
                        val items = repo.fetchMusicItems(country, language, genre)
                        if (items.isEmpty()) {
                            setBlankState()
                            showInlineNoDataMessage(noDataMessageForCurrentTrend())
                            Toast.makeText(this@TrendDetailActivity, noDataMessageForCurrentTrend(), Toast.LENGTH_LONG).show()
                        } else {
                            renderMedia(items)
                        }
                    }
                    TrendType.MOVIES -> {
                        val items = repo.fetchMovieItems(country, language, genre)
                        if (items.isEmpty()) {
                            setBlankState()
                            showInlineNoDataMessage(noDataMessageForCurrentTrend())
                            Toast.makeText(this@TrendDetailActivity, noDataMessageForCurrentTrend(), Toast.LENGTH_LONG).show()
                        } else {
                            renderMedia(items)
                        }
                    }
                    TrendType.PODCAST -> {
                        val items = repo.fetchPodcastItems(country, language, genre)
                        if (items.isEmpty()) {
                            setBlankState()
                            showInlineNoDataMessage(noDataMessageForCurrentTrend())
                            Toast.makeText(this@TrendDetailActivity, noDataMessageForCurrentTrend(), Toast.LENGTH_LONG).show()
                        } else {
                            renderMedia(items)
                        }
                    }
                    else -> {
                        if (trendType == TrendType.WEATHER) {
                            val bundle = repo.fetchWeatherBundle(
                                countryCode = country,
                                locationQuery = location,
                                historyDays = days
                            )
                            if (bundle.historical.isEmpty() && bundle.forecast.isEmpty()) {
                                setBlankState()
                                showInlineNoDataMessage(noDataMessageForCurrentTrend())
                                Toast.makeText(this@TrendDetailActivity, noDataMessageForCurrentTrend(), Toast.LENGTH_LONG).show()
                            } else {
                                if (bundle.historical.isNotEmpty()) render(bundle.historical, trendType.label)
                                forecastAdapter.submitList(bundle.forecast)
                            }
                        } else {
                            val points = repo.fetch(
                                type = trendType,
                                countryCode = country,
                                languageCode = language,
                                genreId = genre,
                                locationQuery = location,
                                days = days
                            )

                            if (points.isEmpty()) {
                                setBlankState()
                                showInlineNoDataMessage(noDataMessageForCurrentTrend())
                                Toast.makeText(this@TrendDetailActivity, noDataMessageForCurrentTrend(), Toast.LENGTH_LONG).show()
                            } else {
                                render(points, trendType.label)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                setBlankState()
                Toast.makeText(
                    this@TrendDetailActivity,
                    getString(R.string.trend_load_error, e.message ?: "unknown error"),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressTrend.visibility = if (loading) View.VISIBLE else View.GONE
        binding.buttonFetchTrend.isEnabled = !loading
    }

    private fun setBlankState() {
        binding.textTrendLatest.text = getString(R.string.trend_placeholder_latest)
        binding.textTrendChange.text = getString(R.string.trend_placeholder_change)
        binding.textTrendMin.text = getString(R.string.trend_placeholder_min)
        binding.textTrendMax.text = getString(R.string.trend_placeholder_max)
        binding.textTrendChange.setTextColor(getColor(R.color.colorTextSecondary))
        binding.textEmptyStateTrend.visibility = View.GONE
        binding.textEmptyStateTrend.text = ""
        rateAdapter.submitList(emptyList())
        mediaAdapter.submitList(emptyList())
        if (binding.cardChartTrend.visibility == View.VISIBLE) {
            binding.trendChart.clear()
            binding.trendChart.invalidate()
        }
        forecastAdapter.submitList(emptyList())
    }

    private fun selectedPeriodDays(): Int {
        val selected = binding.spinnerPeriodTrend.selectedItem as? OptionItem
        return selected?.code?.toIntOrNull() ?: 1
    }

    private fun selectedCode(selected: OptionItem?, fallback: String): String =
        selected?.code ?: fallback

    private fun noDataMessageForCurrentTrend(): String = when (trendType) {
        TrendType.MOVIES -> getString(R.string.trend_no_data_movies)
        TrendType.MUSIC -> getString(R.string.trend_no_data_music)
        TrendType.MARKET -> getString(R.string.trend_no_data_market)
        TrendType.PODCAST -> getString(R.string.trend_no_data_podcast)
        else -> getString(R.string.trend_no_data)
    }

    private fun makeAdapter(items: List<OptionItem>): ArrayAdapter<OptionItem> =
        ArrayAdapter(this, R.layout.spinner_item, items).also {
            it.setDropDownViewResource(R.layout.spinner_dropdown_item)
        }

    private fun updateLanguagesForCountry(countryCode: String) {
        val items = TrendOptions.languagesForCountry(countryCode)
        languageAdapter = makeAdapter(items)
        binding.spinnerLanguageTrend.adapter = languageAdapter
        binding.spinnerLanguageTrend.setSelection(0)
    }

    private fun updateMusicGenresForCountry(countryCode: String) {
        val items = TrendOptions.musicGenresForCountry(countryCode)
        genreAdapter = makeAdapter(items)
        binding.spinnerGenreTrend.adapter = genreAdapter
        binding.spinnerGenreTrend.setSelection(0)
    }

    private fun updateMusicColumnHeaders(countryCode: String) {
        if (isSouthAsianCountry(countryCode)) {
            binding.textColRight.setText(R.string.trend_col_from_movie)
        } else {
            binding.textColRight.setText(R.string.trend_col_singer)
        }
    }

    private fun isSouthAsianCountry(countryCode: String): Boolean = when (countryCode.lowercase()) {
        "in", "pk", "bd", "lk", "np", "bt", "mv" -> true
        else -> false
    }

    private fun updateMarketIndexesForCountry(countryCode: String) {
        val items = TrendOptions.marketIndexesForCountry(countryCode)
        genreAdapter = makeAdapter(items)
        binding.spinnerGenreTrend.adapter = genreAdapter
        binding.spinnerGenreTrend.setSelection(0)
    }

    private fun updateWeatherStatesAndCities(countryCode: String) {
        val countryName = (binding.spinnerCountryTrend.selectedItem as? OptionItem)?.label ?: countryCode
        updateWeatherStatesAndCities(countryCode, countryName)
    }

    private fun updateWeatherStatesAndCities(countryCode: String, countryName: String) {
        lifecycleScope.launch {
            val states = regionRepo.getStates(countryCode, countryName)
            stateAdapter = makeAdapter(states)
            binding.spinnerStateTrend.adapter = stateAdapter
            binding.spinnerStateTrend.setSelection(0)

            val firstState = states.firstOrNull()?.code.orEmpty()
            updateWeatherCities(countryCode, countryName, firstState)
        }
    }

    private fun updateWeatherCities(countryCode: String, countryName: String, state: String) {
        lifecycleScope.launch {
            val cities = regionRepo.getCities(countryCode, countryName, state)
            cityAdapter = makeAdapter(cities)
            binding.spinnerCityTrend.adapter = cityAdapter
            binding.spinnerCityTrend.setSelection(0)
        }
    }

    private fun loadCountries() {
        lifecycleScope.launch {
            val countries = regionRepo.getCountries().ifEmpty { TrendOptions.countries }
            countryAdapter = makeAdapter(countries)
            binding.spinnerCountryTrend.adapter = countryAdapter

            val defaultCode = if (trendType == TrendType.WEATHER) "us" else (countries.firstOrNull()?.code ?: "us")
            val selectedIndex = countries.indexOfFirst { it.code.equals(defaultCode, ignoreCase = true) }.takeIf { it >= 0 } ?: 0
            binding.spinnerCountryTrend.setSelection(selectedIndex)
        }
    }

    private fun setupChart() {
        val darkMode = isDarkTheme()
        val chartBg = if (darkMode) Color.parseColor("#121820") else Color.WHITE
        val grid = if (darkMode) Color.parseColor("#2E3A47") else Color.parseColor("#E0E0E0")
        val axisText = if (darkMode) Color.WHITE else Color.parseColor("#424242")

        with(binding.trendChart) {
            setTouchEnabled(true)
            setPinchZoom(true)
            description.isEnabled = false
            setDrawGridBackground(false)
            setBackgroundColor(chartBg)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = -45f
                granularity = 1f
                setDrawGridLines(true)
                gridColor = grid
                textColor = axisText
                textSize = 10f
            }
            axisLeft.apply {
                granularity = 0.1f
                setDrawGridLines(true)
                gridColor = grid
                textColor = axisText
            }
            axisRight.isEnabled = false
            legend.textColor = axisText
            legend.textSize = 12f
            animateX(600)
        }
    }

    private fun isDarkTheme(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    private fun render(data: List<CurrencyRate>, label: String) {
        if (data.isEmpty()) return

        binding.textEmptyStateTrend.visibility = View.GONE
        binding.textEmptyStateTrend.text = ""

        val dateLabels = data.map { it.date.takeLast(5) }
        val entries = data.mapIndexed { i, p -> Entry(i.toFloat(), p.rate.toFloat()) }

        val dataSet = LineDataSet(entries, label).apply {
            color = Color.parseColor("#1565C0")
            lineWidth = 2.5f
            circleRadius = 3f
            setCircleColor(Color.parseColor("#1565C0"))
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = Color.parseColor("#1565C0")
            fillAlpha = 30
            mode = LineDataSet.Mode.CUBIC_BEZIER
            highLightColor = Color.parseColor("#FF6D00")
        }

        binding.trendChart.xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels)
        binding.trendChart.data = LineData(dataSet)
        binding.trendChart.invalidate()

        val min = data.minOf { it.rate }
        val max = data.maxOf { it.rate }
        val first = data.first().rate
        val last = data.last().rate
        val pct = if (first == 0.0) 0.0 else (last - first) / first * 100.0
        val sign = if (pct >= 0) "▲ +" else "▼ "

        binding.textTrendLatest.text = getString(R.string.trend_latest, last)
        binding.textTrendMin.text = getString(R.string.trend_min, min)
        binding.textTrendMax.text = getString(R.string.trend_max, max)
        binding.textTrendChange.text = getString(R.string.trend_change, sign, abs(pct))
        binding.textTrendChange.setTextColor(
            if (isDarkTheme()) Color.WHITE
            else if (pct >= 0) Color.parseColor("#2E7D32")
            else Color.parseColor("#C62828")
        )

        rateAdapter.submitList(data.reversed())
    }

    private fun renderMedia(items: List<MediaTrendItem>) {
        if (items.isEmpty()) {
            setBlankState()
            return
        }

        binding.textEmptyStateTrend.visibility = View.GONE
        binding.textEmptyStateTrend.text = ""

        val first = items.first().score
        val last = items.last().score
        val min = items.minOf { it.score }
        val max = items.maxOf { it.score }
        val pct = if (first == 0.0) 0.0 else (last - first) / first * 100.0
        val sign = if (pct >= 0) "▲ +" else "▼ "

        binding.textTrendLatest.text = getString(R.string.trend_latest, first)
        binding.textTrendMin.text = getString(R.string.trend_min, min)
        binding.textTrendMax.text = getString(R.string.trend_max, max)
        binding.textTrendChange.text = getString(R.string.trend_change, sign, abs(pct))
        binding.textTrendChange.setTextColor(
            if (pct >= 0) Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
        )

        mediaAdapter.submitList(items)
    }

    private fun showInlineNoDataMessage(message: String) {
        binding.textEmptyStateTrend.text = message
        binding.textEmptyStateTrend.visibility = View.VISIBLE
    }
}
