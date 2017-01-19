package com.zealous.bankRates;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * @author by yaaminu on 1/18/17.
 */
public class SimpleInterestRateCalculator extends BaseInterestRateCalculator {

    @NonNull
    @Override
    protected BigDecimal doGetInterest() {
        return BigDecimal.valueOf(principal)
                .multiply(BigDecimal.valueOf(rate), MathContext.DECIMAL128)
                .multiply(BigDecimal.valueOf(duration));
    }

    @Override
    protected BigDecimal doGetAmount() {
        return BigDecimal.valueOf(principal).add(getInterest(), MathContext.DECIMAL128);
    }

}
