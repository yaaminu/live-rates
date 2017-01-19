package com.zealous.bankRates;

import android.support.annotation.NonNull;

import com.zealous.utils.GenericUtils;
import com.zealous.utils.ThreadUtils;

import java.math.BigDecimal;

/**
 * by yaaminu on 1/18/17.
 */
public class BaseInterestRateCalculator implements InterestCalculator {
    private static final int INVALID_VALUE = -1;
    protected double principal;
    protected double rate;
    protected double duration;
    protected double amount = INVALID_VALUE;
    protected double interest = INVALID_VALUE;

    private void ensureMain() {
        ThreadUtils.ensureMain();
    }

    @NonNull
    @Override
    public final BigDecimal getInterest() {
        ensureMain();
        if (principal <= 0 || rate <= 0 || duration <= 0) {
            amount = INVALID_VALUE;
            interest = INVALID_VALUE;
            return BigDecimal.ZERO;
        }
        if (interest != INVALID_VALUE) {
            return BigDecimal.valueOf(interest);
        }
        BigDecimal tmp = doGetInterest();
        interest = tmp.doubleValue();
        return tmp;
    }


    @NonNull
    @Override
    public final BigDecimal getAmount() {
        ensureMain();
        if (principal <= 0 || rate <= 0 || duration <= 0) {
            amount = INVALID_VALUE;
            interest = INVALID_VALUE;
            return BigDecimal.ZERO;
        }
        if (amount != INVALID_VALUE) {
            return BigDecimal.valueOf(amount);
        }
        BigDecimal ret = doGetAmount();
        amount = ret.doubleValue();
        return ret;
    }

    protected BigDecimal doGetAmount() {
        throw new UnsupportedOperationException();
    }

    protected BigDecimal doGetInterest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDuration() {
        ensureMain();
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        ensureMain();
        GenericUtils.ensureConditionTrue(duration >= 0, "must be >= 0");
        if (this.duration != duration) {
            amount = INVALID_VALUE;
            interest = INVALID_VALUE;
        }
        this.duration = duration;
    }

    @Override
    public double getRate() {
        ensureMain();
        return rate;
    }

    @Override
    public void setRate(double rate) {
        ensureMain();
        GenericUtils.ensureConditionTrue(rate >= 0, "must be >= 0");
        if (this.rate != rate) {
            amount = INVALID_VALUE;
            interest = INVALID_VALUE;
        }
        this.rate = rate;
    }

    @Override
    public double getPrincipal() {
        ensureMain();
        return principal;
    }

    @Override
    public void setPrincipal(double principal) {
        ensureMain();
        GenericUtils.ensureConditionTrue(principal >= 0, "must be >= 0");
        if (principal != this.principal) {
            amount = INVALID_VALUE;
            interest = INVALID_VALUE;
        }
        this.principal = principal;
    }

}
