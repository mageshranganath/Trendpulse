package com.trendpulse.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trendpulse.app.models.CurrencyOption
import com.trendpulse.app.models.CurrencyRate
import kotlinx.coroutines.launch

class CurrencyViewModel : ViewModel() {

    private val repo = CurrencyRepository()

    val rates      = MutableLiveData<List<CurrencyRate>>(emptyList())
    val currencies = MutableLiveData<List<CurrencyOption>>(emptyList())
    val isLoading  = MutableLiveData(false)
    val error      = MutableLiveData<String?>(null)

    fun loadCurrencies() {
        viewModelScope.launch {
            try {
                currencies.value = repo.getCurrencies()
            } catch (e: Exception) {
                error.value = "Could not load currency list: ${e.message}"
            }
        }
    }

    fun loadRates(base: String, target: String, days: Int) {
        if (base == target) {
            error.value = "Base and target currency must be different"
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            try {
                rates.value = repo.getHistoricalRates(base, target, days)
            } catch (e: Exception) {
                error.value = "Could not load rates: ${e.message}"
                rates.value = emptyList()
            } finally {
                isLoading.value = false
            }
        }
    }
}
