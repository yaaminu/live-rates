package com.zealous.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.zealous.equity.LineChartEntry
import com.zealous.exchangeRates.ExchangeRate
import com.zealous.stock.Equity
import com.zealous.utils.Config
import io.realm.Realm

class HomeViewModel : ViewModel() {

    private val exchangeRateRealm: Realm = ExchangeRate.Realm(Config.getApplicationContext())
    private val stockRealm: Realm = Realm.getDefaultInstance()
    private val watchedCurrencies: WatchedCurrencies = WatchedCurrencies(exchangeRateRealm)
    private val watchedStocks: WatchedStock = WatchedStock(stockRealm)
    private val selectedCurrencyIndex: MutableLiveData<Int> = MutableLiveData()
    private val converter = Converter(null, 0.0)
    private val homeHistoricalExchangeRates = HomeHistoricalExchangeRates(emptyList())
    private val homHistoricalEquities = HomHistoricalEquities(emptyList())

    init {
        selectedCurrencyIndex.value = 0
    }

    fun getSelectedCurrencyIndex(): LiveData<Int> {
        return selectedCurrencyIndex
    }

    fun updateInput(input: String) {
        converter.input = try {
            java.lang.Double.parseDouble(input)
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    fun getExchangeRateByIndex(position: Int): ExchangeRate? {
        return watchedCurrencies.value?.get(position)
    }

    fun updateSelectedItem(position: Int) {
        if (selectedCurrencyIndex.value != position) {
            selectedCurrencyIndex.value = position
        }
        if (converter.currency != watchedCurrencies.value?.get(position)) {
            converter.currency = watchedCurrencies.value?.get(position)
        }
    }

    fun getWatchedCurrencies(): LiveData<List<ExchangeRate>> = watchedCurrencies

    fun getHistoricalRatesForWatchedCurrencies(currencies: List<ExchangeRate>): LiveData<Map<String, List<LineChartEntry>>> {
        if (homeHistoricalExchangeRates.currencies.isEmpty()) {
            homeHistoricalExchangeRates.currencies = exchangeRateRealm.copyFromRealm(currencies)
        }
        return homeHistoricalExchangeRates
    }

    override fun onCleared() {
        exchangeRateRealm.close()
        stockRealm.close()
        super.onCleared()
    }

    fun getWatchedStock(): LiveData<List<Equity>> = watchedStocks
    fun getHistoricalRatesForWatchedEquities(equities: List<Equity>): LiveData<Map<String, List<LineChartEntry>>> {
        if (homHistoricalEquities.equities.isEmpty()) {
            homHistoricalEquities.equities = stockRealm.copyFromRealm(equities)
        }
        return homHistoricalEquities
    }


    fun getConvertedValue(): LiveData<String> {
        return converter
    }
}