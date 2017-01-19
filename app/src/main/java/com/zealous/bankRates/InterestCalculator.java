package com.zealous.bankRates;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

/**
 * by yaaminu on 1/18/17.
 */
interface InterestCalculator {
    @NonNull
    BigDecimal getInterest();

    @NonNull
    BigDecimal getAmount();

    double getDuration();

    void setDuration(double duration);

    double getRate();

    void setRate(double rate);

    double getPrincipal();

    void setPrincipal(double principal);
}
