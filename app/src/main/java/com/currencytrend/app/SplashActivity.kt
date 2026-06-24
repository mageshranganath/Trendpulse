package com.trendpulse.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DELAY_MS = 2200L
    }

    private val handler = Handler(Looper.getMainLooper())

    private val launchRunnable = Runnable {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        handler.postDelayed(launchRunnable, SPLASH_DELAY_MS)
    }

    override fun onDestroy() {
        handler.removeCallbacks(launchRunnable)
        super.onDestroy()
    }
}
