package com.trendpulse.app

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.trendpulse.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setupThemeToggle()
        setupTrendNavigation()
    }

    override fun onResume() {
        super.onResume()
        updateThemeButtonLabel()
    }

    private fun setupThemeToggle() {
        updateThemeButtonLabel()
        binding.buttonThemeMode.setOnClickListener {
            val darkNow = ThemeModeManager.isDarkMode(this)
            ThemeModeManager.setDarkMode(this, !darkNow)
        }
    }

    private fun updateThemeButtonLabel() {
        val darkNow = ThemeModeManager.isDarkMode(this)
        binding.buttonThemeMode.text = getString(
            if (darkNow) R.string.btn_theme_light else R.string.btn_theme_dark
        )
        val textColor = if (darkNow) {
            ContextCompat.getColor(this, android.R.color.white)
        } else {
            ContextCompat.getColor(this, R.color.colorTextPrimary)
        }
        binding.buttonThemeMode.setTextColor(textColor)

        val backgroundColor = if (darkNow) {
            ContextCompat.getColor(this, R.color.colorPrimary)
        } else {
            ContextCompat.getColor(this, android.R.color.transparent)
        }
        binding.buttonThemeMode.backgroundTintList = ColorStateList.valueOf(backgroundColor)

        if (darkNow) {
            binding.buttonThemeMode.strokeWidth = resources.getDimensionPixelSize(R.dimen.theme_toggle_stroke_width)
            binding.buttonThemeMode.strokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(this, android.R.color.white)
            )
        } else {
            binding.buttonThemeMode.strokeWidth = 0
        }
    }

    private fun setupTrendNavigation() {
        binding.buttonCurrencyTrend.setOnClickListener { startActivity(Intent(this, CurrencyTrendActivity::class.java)) }

        binding.buttonWeatherTrend.setOnClickListener { openTrend(TrendType.WEATHER) }
        binding.buttonMarketTrend.setOnClickListener { openTrend(TrendType.MARKET) }
        binding.buttonMusicTrend.setOnClickListener { openTrend(TrendType.MUSIC) }
        binding.buttonMoviesTrend.setOnClickListener { openTrend(TrendType.MOVIES) }
        binding.buttonPodcastTrend.setOnClickListener { openTrend(TrendType.PODCAST) }
    }

    private fun openTrend(type: TrendType) {
        val intent = Intent(this, TrendDetailActivity::class.java)
            .putExtra(TrendDetailActivity.EXTRA_TREND_TYPE, type.name)
        startActivity(intent)
    }
}
