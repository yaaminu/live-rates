package com.zealous.exchangeRates;

import android.support.annotation.NonNull;

import com.zealous.adapter.ITuple;
import com.zealous.utils.GenericUtils;

/**
 * Created by yaaminu on 1/5/17.
 */
public class HistoricalRateTuple implements ITuple {
    public final double rate;
    @NonNull
    public final String from;
    @NonNull
    public final String to;
    public int index;

    public HistoricalRateTuple(@NonNull String to, @NonNull String from, double rate, int index) {
        GenericUtils.ensureNotEmpty(to, from);
        this.from = from;
        this.to = to;
        this.rate = rate;
        this.index = index;
    }
}
