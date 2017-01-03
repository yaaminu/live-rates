package com.zealous.exchangeRates;

import android.content.Context;

import com.zealous.adapter.ITuple;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by yaaminu on 12/20/16.
 */
public class ExchangeRate extends RealmObject implements ITuple {

    public static final String FIELD_CURRENCY_ISO = "currencyIso";
    public static final String FIELD_WATCHING = "watching";

    @PrimaryKey
    private String currencyIso;

    private double rate;
    @Required
    private String currencyName;

    @Required
    private String currencySymbol;
    private String namePlural;

    private boolean watching;

    public ExchangeRate() {
    }

    public ExchangeRate(String currIso, String currencyName, String currencySymbol, String namePlural, boolean watching) {
        this.currencyIso = currIso;
        this.currencyName = currencyName;
        this.currencySymbol = currencySymbol;
        this.namePlural = namePlural;
        this.watching = watching;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
//
//    @Override
//    public String getFirst() {
//        return currencyName;
//    }
//
//    @Override
//    public String getSecond() {
//        return currencySymbol + "  " + rate;
//    }

    public double getRate() {
        return rate;
    }

    public String getCurrencyIso() {
        return currencyIso;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public String getNamePlural() {
        return namePlural;
    }

    public boolean isWatching() {
        return watching;
    }

    public static Realm Realm(Context context) {
        return Realm.getInstance(new RealmConfiguration
                .Builder()
                .directory(context.getDir("exchange_rates.realm", Context.MODE_PRIVATE))
                .deleteRealmIfMigrationNeeded().build());
    }
}
