package com.trendpulse.app

import android.app.Application

class TrendPulseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeModeManager.applySavedMode(this)
    }
}
