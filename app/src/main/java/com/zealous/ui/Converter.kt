package com.zealous.ui

import android.arch.lifecycle.LiveData
import com.zealous.exchangeRates.ExchangeRate
import java.math.BigDecimal
import java.math.MathContext

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