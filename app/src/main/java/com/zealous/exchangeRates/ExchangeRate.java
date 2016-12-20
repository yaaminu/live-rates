package com.zealous.exchangeRates;

import com.zealous.adapter.ITuple;

import io.realm.annotations.PrimaryKey;

/**
 * Created by yaaminu on 12/20/16.
 */
public class ExchangeRate implements ITuple {
    @PrimaryKey
    public final String currency;
    public final double rate;
    public final String currencyName;
    public final String currencySymbol;

    public ExchangeRate(String currIso, String currencyName, String currencySymbol, double rate) {
        this.currency = currIso;
        this.rate = rate;
        this.currencyName = currencyName;
        this.currencySymbol = currencySymbol;
    }

    @Override
    public String getFirst() {
        return currencyName;
    }

    @Override
    public String getSecond() {
        return currencySymbol + "  " + rate;
    }
}
