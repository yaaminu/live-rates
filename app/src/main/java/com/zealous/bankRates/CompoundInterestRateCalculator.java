package com.zealous.bankRates;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * by yaaminu on 1/19/17.
 */
public class CompoundInterestRateCalculator extends BaseInterestRateCalculator {
    protected BigDecimal doGetAmount() {
        //       interest(I) =  P (1 + r/n) ^(nt) - P
        BigDecimal p = BigDecimal.valueOf(this.principal),
                r = BigDecimal.valueOf(this.rate),
                t = BigDecimal.valueOf(this.duration),
                n = BigDecimal.valueOf(12); // months;
        double tmp = BigDecimal.ONE.add(r.divide(n, MathContext.DECIMAL128)).doubleValue();
        tmp = Math.pow(tmp, t.multiply(n, MathContext.DECIMAL128)
                .doubleValue());
        return p.multiply(BigDecimal.valueOf(tmp), MathContext.DECIMAL128);
    }

    protected BigDecimal doGetInterest() {
        BigDecimal tmp = getAmount();
        if (tmp.doubleValue() == 0) return BigDecimal.ZERO;
        return tmp.subtract(BigDecimal.valueOf(principal), MathContext.DECIMAL128);
    }
}
