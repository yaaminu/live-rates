package com.zealous.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.zealous.R
import com.zealous.equity.LineChartEntry
import com.zealous.exchangeRates.ExchangeRate
import com.zealous.stock.Equity
import com.zealous.utils.Config
import io.realm.Realm
import rx.Observable
import java.math.BigDecimal
import java.math.MathContext
import java.security.SecureRandom

class HomeFragmentParent : BaseFragment() {
    override fun getLayout(): Int {
        return R.layout.fragment_home
    }

}

class HomeViewModel : ViewModel() {
    private val exchangeRateRealm: Realm = ExchangeRate.Realm(Config.getApplicationContext())
    private val stockRealm: Realm = Realm.getDefaultInstance()
    private val watchedCurrencies: WatchedCurrencies = WatchedCurrencies(exchangeRateRealm)
    private val watchedStocks: WatchedStock = WatchedStock(stockRealm)
    private val selectedCurrencyIndex: MutableLiveData<Int> = MutableLiveData()
    private val converter = Converter(null, 0.0)

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

    fun getHistoricalRatesForWatchedCurrencies(currencies: List<ExchangeRate>): Observable<Pair<String, List<LineChartEntry>>> {

        return Observable.from(exchangeRateRealm.copyFromRealm(currencies))
                .map {
                    it.currencyIso
                }
                .flatMap {
                    loadLast30DaysForCurrency(it)
                }.map {
                    val tmp: MutableList<LineChartEntry> = ArrayList(it.second.size)

                    for ((index, i) in it.second.withIndex()) {
                        tmp.add(LineChartEntry("${index + 1}", index.toFloat(), i.toFloat(), System.currentTimeMillis()))
                    }
                    it.first to tmp
                }
    }


    fun loadLast30DaysForCurrency(currency: String): Observable<Pair<String, List<Double>>> {
        val list: MutableList<Double> = ArrayList(30)
        0.until(30).forEach {
            list.add(getRandomRate().toDouble())
        }
        return Observable.just(currency to list)
    }

    fun getRandomRate(): Float {
        val random = SecureRandom()
        //maximum of 10000 and minimum of 99999
        val num = Math.abs(random.nextDouble() * (8 - 4) + 4)
        //we need an unsigned (+ve) number
        return Math.abs(num).toFloat()
    }

    override fun onCleared() {
        exchangeRateRealm.close()
        stockRealm.close()
        super.onCleared()
    }

    fun getWatchedStock(): LiveData<List<Equity>> = watchedStocks
    fun getHistoricalRatesForWatchedEquities(equities: List<Equity>): Observable<Pair<String, List<LineChartEntry>>> {
        return Observable.from(stockRealm.copyFromRealm(equities))
                .map {
                    it.symbol
                }
                .flatMap {
                    loadLast7DaysQoutes(it)
                }.map {
                    val tmp: MutableList<LineChartEntry> = ArrayList(it.second.size)

                    for ((index, i) in it.second.withIndex()) {
                        tmp.add(LineChartEntry("${index + 1}", index.toFloat(), i.toFloat(), System.currentTimeMillis()))
                    }
                    it.first to tmp
                }
    }

    private fun loadLast7DaysQoutes(symbol: String): Observable<Pair<String, List<Double>>> {
        val list: MutableList<Double> = ArrayList(7)
        0.until(7).forEach {
            list.add(getRandomRate().toDouble())
        }
        return Observable.just(symbol to list)
    }

    fun getConvertedValue(): LiveData<String> {
        return converter
    }
}

class Converter(currency: ExchangeRate?, input: Double) : LiveData<String>() {
    var input = input
        set(value) {
            field = value
            setValue(if (currency == null || value == 0.0) "" else ExchangeRate.FORMAT.format(BigDecimal.valueOf(value)
                    .divide(BigDecimal.valueOf(currency!!.rate), MathContext.DECIMAL128)))
        }
    var currency = currency
        set(value) {
            field = value
            setValue(if (value == null || input == 0.0) "" else
                ExchangeRate.FORMAT.format(BigDecimal.valueOf(input)
                        .divide(BigDecimal.valueOf(value.rate), MathContext.DECIMAL128)))
        }

}