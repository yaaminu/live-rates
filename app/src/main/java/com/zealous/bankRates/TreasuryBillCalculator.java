package com.zealous.bankRates;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * by yaaminu on 1/18/17.
 */
@SuppressWarnings("WeakerAccess")
public class TreasuryBillCalculator extends BaseInterestRateCalculator {

    @NonNull
    @Override
    protected BigDecimal doGetInterest() {
        return BigDecimal.valueOf(duration)
                .multiply(BigDecimal.valueOf(rate * 100), MathContext.DECIMAL128);
    }

    @NonNull
    @Override
    protected BigDecimal doGetAmount() {
        return BigDecimal.valueOf(principal).add(getInterest(), MathContext.DECIMAL128);
    }

}
