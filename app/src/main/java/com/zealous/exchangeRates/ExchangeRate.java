package com.zealous.exchangeRates;

import android.content.Context;
import android.support.annotation.DrawableRes;

import com.zealous.R;
import com.zealous.adapter.ITuple;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * by yaaminu on 12/20/16.
 */
public class ExchangeRate extends RealmObject implements ITuple {

    public static final String FIELD_CURRENCY_ISO = "currencyIso";
    public static final String FIELD_WATCHING = "watching";
    public static final String FIELD_CURRENCY_NAME = "currencyName";
    public static final NumberFormat FORMAT = DecimalFormat.getNumberInstance();

    static {
        FORMAT.setMaximumFractionDigits(2);
        FORMAT.setMinimumFractionDigits(2);
        FORMAT.setRoundingMode(RoundingMode.HALF_UP);
    }

    @PrimaryKey
    private String currencyIso;
    private double rate;
    @Required
    private String currencyName;
    @Required
    private String currencySymbol;
    private String namePlural;
    private byte watching;

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

    public ExchangeRate() {
    }

    public ExchangeRate(String currIso, String currencyName, String currencySymbol, String namePlural, boolean watching) {
        this.currencyIso = currIso;
        this.currencyName = currencyName;
        this.currencySymbol = currencySymbol;
        this.namePlural = namePlural;
        this.watching = (byte) (watching ? 1 : 0);
    }

    public static Realm Realm(Context context) {
        return Realm.getInstance(new RealmConfiguration
                .Builder()
                .directory(context.getDir("exchange_rates.realm", Context.MODE_PRIVATE))
                .deleteRealmIfMigrationNeeded().build());
    }

    public void setWatching() {
        this.watching = 1;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
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
        return watching == 1;
    }

    @DrawableRes
    public int getCurrencyIcon(Context context) {
        int res = context.getResources().getIdentifier("drawable/" +
                        getCurrencyIso().toLowerCase(Locale.US),
                null, context.getPackageName());
        if (res == 0) {
            res = R.drawable.default_flag;
        }
        return res;
    }
}
