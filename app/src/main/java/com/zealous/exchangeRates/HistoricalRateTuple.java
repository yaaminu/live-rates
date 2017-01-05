package com.zealous.exchangeRates;

import com.zealous.adapter.ITuple;

/**
 * Created by yaaminu on 1/5/17.
 */
public class HistoricalRateTuple implements ITuple {
    public final String second;
    public final String first;

    public HistoricalRateTuple(String first, String second) {
        this.first = first;
        this.second = second;
    }
}
