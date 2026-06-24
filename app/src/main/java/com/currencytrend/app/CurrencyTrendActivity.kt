package com.trendpulse.app

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.os.Bundle
import android.content.res.Configuration
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.trendpulse.app.databinding.ActivityCurrencyTrendBinding
import com.trendpulse.app.models.CurrencyOption
import com.trendpulse.app.models.CurrencyRate
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class CurrencyTrendActivity : BaseTrendActivity() {

    private lateinit var binding: ActivityCurrencyTrendBinding
    private val viewModel: CurrencyViewModel by viewModels()
    private val rateAdapter = RateAdapter()
    private val periodDays = listOf(1, 7, 14, 30, 60, 90)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrencyTrendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbarBack(binding.toolbar)
        supportActionBar?.title = getString(R.string.btn_currency_trend)

        setupRecyclerView()
        setupChart()
        setupPeriodSpinner()
        setupSwapButton()
        setupFetchButton()
        setupCopyLatestButton()
        setupObservers()
        setBlankState()

        viewModel.loadCurrencies()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = rateAdapter
        binding.recyclerView.isNestedScrollingEnabled = true
    }

    private fun setupChart() {
        val darkMode = isDarkTheme()
        val chartBg = if (darkMode) Color.parseColor("#121820") else Color.WHITE
        val grid = if (darkMode) Color.parseColor("#2E3A47") else Color.parseColor("#E0E0E0")
        val axisText = if (darkMode) Color.WHITE else Color.parseColor("#424242")

        with(binding.lineChart) {
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
                granularity = 0.0001f
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

    private fun setupPeriodSpinner() {
        val labels = periodDays.map { if (it == 1) "Today" else "$it days" }
        binding.spinnerPeriod.adapter =
            ArrayAdapter(this, R.layout.spinner_item, labels).also {
                it.setDropDownViewResource(R.layout.spinner_dropdown_item)
            }
        binding.spinnerPeriod.setSelection(0)
    }

    private fun setupSwapButton() {
        binding.buttonSwap.setOnClickListener {
            val basePos = binding.spinnerBase.selectedItemPosition
            val targetPos = binding.spinnerTarget.selectedItemPosition
            if (basePos < 0 || targetPos < 0) return@setOnClickListener

            binding.spinnerBase.setSelection(targetPos)
            binding.spinnerTarget.setSelection(basePos)

            val base = (binding.spinnerBase.selectedItem as? CurrencyOption)?.code ?: return@setOnClickListener
            val target = (binding.spinnerTarget.selectedItem as? CurrencyOption)?.code ?: return@setOnClickListener
            val days = periodDays.getOrElse(binding.spinnerPeriod.selectedItemPosition) { 30 }
            viewModel.loadRates(base, target, days)
        }
    }

    private fun setupFetchButton() {
        binding.buttonFetch.setOnClickListener {
            val base = (binding.spinnerBase.selectedItem as? CurrencyOption)?.code ?: return@setOnClickListener
            val target = (binding.spinnerTarget.selectedItem as? CurrencyOption)?.code ?: return@setOnClickListener
            val days = periodDays.getOrElse(binding.spinnerPeriod.selectedItemPosition) { 30 }
            viewModel.loadRates(base, target, days)
        }
    }

    private fun setupCopyLatestButton() {
        binding.buttonCopyLatest.setOnClickListener {
            val rates = viewModel.rates.value.orEmpty()
            if (rates.isEmpty()) return@setOnClickListener

            val latest = rates.last().rate
            val base = (binding.spinnerBase.selectedItem as? CurrencyOption)?.code ?: ""
            val target = (binding.spinnerTarget.selectedItem as? CurrencyOption)?.code ?: ""
            val value = "${"%.4f".format(latest)}"
            val text = if (base.isNotBlank() && target.isNotBlank()) "$base/$target: $value" else value

            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("latest_rate", text))
            Toast.makeText(this, "Latest rate copied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        viewModel.currencies.observe(this) { list ->
            if (list.isNotEmpty()) populateCurrencySpinners(list)
        }
        viewModel.rates.observe(this) { rates ->
            if (rates.isNotEmpty()) {
                renderChart(rates)
                renderStats(rates)
                rateAdapter.submitList(rates.reversed())
            } else {
                setBlankState()
            }
        }
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.buttonFetch.isEnabled = !loading
        }
        viewModel.error.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun populateCurrencySpinners(list: List<CurrencyOption>) {
        val adapter = ArrayAdapter(this, R.layout.spinner_item, list).also {
            it.setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
        binding.spinnerBase.adapter = adapter
        binding.spinnerTarget.adapter = adapter

        val cadIdx = list.indexOfFirst { it.code == "CAD" }.takeIf { it >= 0 } ?: 0
        val inrIdx = list.indexOfFirst { it.code == "INR" }.takeIf { it >= 0 } ?: 1
        binding.spinnerBase.setSelection(cadIdx)
        binding.spinnerTarget.setSelection(inrIdx)
    }

    private fun renderChart(rates: List<CurrencyRate>) {
        val dateLabels = rates.map { it.date.takeLast(5) }
        val entries = rates.mapIndexed { i, r -> Entry(i.toFloat(), r.rate.toFloat()) }

        val base = (binding.spinnerBase.selectedItem as? CurrencyOption)?.code ?: ""
        val target = (binding.spinnerTarget.selectedItem as? CurrencyOption)?.code ?: ""

        val dataSet = LineDataSet(entries, "$base -> $target").apply {
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

        binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels)
        binding.lineChart.data = LineData(dataSet)
        binding.lineChart.invalidate()
        binding.lineChart.animateX(800)
    }

    private fun renderStats(rates: List<CurrencyRate>) {
        val min = rates.minOf { it.rate }
        val max = rates.maxOf { it.rate }
        val first = rates.first().rate
        val last = rates.last().rate
        val pct = (last - first) / first * 100.0

        binding.textLatest.text = "Latest:  %.4f".format(last)
        binding.textMin.text = "Min:  %.4f".format(min)
        binding.textMax.text = "Max:  %.4f".format(max)

        val sign = if (pct >= 0) "▲ +" else "▼ "
        binding.textChange.text = "Change: $sign${"%.2f".format(pct)}%"
        binding.textChange.setTextColor(
            if (isDarkTheme()) Color.WHITE
            else if (pct >= 0) Color.parseColor("#2E7D32")
            else Color.parseColor("#C62828")
        )
        binding.buttonCopyLatest.isEnabled = true
    }

    private fun setBlankState() {
        binding.textLatest.text = getString(R.string.latest_placeholder)
        binding.textChange.text = getString(R.string.trend_placeholder_change)
        binding.textMin.text = getString(R.string.trend_placeholder_min)
        binding.textMax.text = getString(R.string.trend_placeholder_max)
        binding.textChange.setTextColor(Color.parseColor("#888888"))
        binding.buttonCopyLatest.isEnabled = false
        rateAdapter.submitList(emptyList())
        binding.lineChart.clear()
        binding.lineChart.invalidate()
    }
}
