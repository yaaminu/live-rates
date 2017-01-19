package com.zealous.bankRates;

import android.os.Looper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * by yaaminu on 1/19/17.
 */
@PrepareForTest(Looper.class)
public class CompoundInterestRateCalculatorTest {
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    private Thread mainThreadMock;
    private Looper mockLooper;

    @Before
    public void setUp() throws Exception {
        mainThreadMock = new Thread("mainthread");
        mockStatic(Looper.class);
        mockLooper = mock(Looper.class);
        when(Looper.getMainLooper()).thenReturn(mockLooper);
        when(mockLooper.getThread()).thenReturn(Thread.currentThread());
    }

    @Test
    public void getInterest() throws Exception {
        BaseInterestRateCalculator calc = new CompoundInterestRateCalculator();
        assertNotNull(calc.getInterest());
        try {
            switchThread(false);
            calc.getInterest();
            fail("must not allow operation outside the main thread");
        } catch (IllegalStateException e) {
            System.out.println("expected");
        }
        switchThread(true);
        calc.getInterest(); //must not throw now

        calc.setDuration(10);
        calc.setRate(0.05);
        calc.setPrincipal(5000);
        assertEquals(calc.getInterest().doubleValue(),
                BigDecimal.valueOf(8235.05).subtract(BigDecimal.valueOf(5000),
                        MathContext.DECIMAL128).doubleValue(), 0.01);
        calc.setDuration(0);
        assertEquals(calc.getInterest().doubleValue(), 0, 0);
        calc.setDuration(10);
        assertEquals(calc.getInterest().doubleValue(),
                BigDecimal.valueOf(8235.05).subtract(BigDecimal.valueOf(5000),
                        MathContext.DECIMAL128).doubleValue(), 0.01);

        calc.setRate(0);
        assertEquals(calc.getInterest().doubleValue(), 0, 0);
        calc.setRate(0.05);
        assertEquals(calc.getInterest().doubleValue(),
                BigDecimal.valueOf(8235.05).subtract(BigDecimal.valueOf(5000),
                        MathContext.DECIMAL128).doubleValue(), 0.01);

        calc.setPrincipal(0);
        assertEquals(calc.getInterest().doubleValue(), 0, 0);
        calc.setPrincipal(5000);
        assertEquals(calc.getInterest().doubleValue(),
                BigDecimal.valueOf(8235.05).subtract(BigDecimal.valueOf(5000),
                        MathContext.DECIMAL128).doubleValue(), 0.01);
    }

    private void switchThread(boolean isMainThread) {
        when(mockLooper.getThread()).thenReturn(isMainThread
                ? Thread.currentThread() : mainThreadMock);
    }

    @Test
    public void getAmount() throws Exception {
        BaseInterestRateCalculator calc = new CompoundInterestRateCalculator();
        assertNotNull(calc.getAmount());
        try {
            switchThread(false);
            calc.getAmount();
            fail("must not allow operation outside the main thread");
        } catch (IllegalStateException e) {
            System.out.println("expected");
        }
        switchThread(true);
        calc.getAmount(); //must not throw now
//        P = 5000. r = 5/100 = 0.05 (decimal). n = 12. t = 10.

        calc.setDuration(10);
        calc.setRate(0.05);
        calc.setPrincipal(5000);
        assertEquals(calc.getAmount().doubleValue(), 8235.05, 0.01);

        calc.setDuration(0);
        assertEquals(calc.getAmount().doubleValue(), 0, 0);
        calc.setDuration(10);
        assertEquals(calc.getAmount().doubleValue(),
                BigDecimal.valueOf(8235.05).doubleValue(), 0.01);

        calc.setRate(0);
        assertEquals(calc.getAmount().doubleValue(), 0, 0);
        calc.setRate(0.05);
        assertEquals(calc.getAmount().doubleValue(),
                BigDecimal.valueOf(8235.05).doubleValue(), 0.01);

        calc.setPrincipal(0);
        assertEquals(calc.getAmount().doubleValue(), 0, 0);
        calc.setPrincipal(5000);
        assertEquals(calc.getAmount().doubleValue(),
                BigDecimal.valueOf(8235.05).doubleValue(), 0.01);
    }
}