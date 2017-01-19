package com.zealous.bankRates;

import android.os.Looper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * by yaaminu on 1/19/17.
 */
@PrepareForTest({Looper.class, Thread.class})
public class BaseInterestRateCalculatorTest {
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    Thread mainThreadMock;
    private Looper mockLooper;

    @Before
    public void setUp() throws Exception {
        mainThreadMock = new Thread("mainthread");
        mockStatic(Looper.class);
        mockLooper = mock(Looper.class);
        when(Looper.getMainLooper()).thenReturn(mockLooper);
        when(mockLooper.getThread()).thenReturn(Thread.currentThread());
    }


    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void setPrincipal() throws Exception {
        InterestCalculator calc = new BaseInterestRateCalculator();
        try {
            calc.setPrincipal(-1.89);
            calc.setPrincipal(-100);
            calc.setPrincipal(-0.001);
            calc.setPrincipal(-0.1);
            fail("must not accept negative values");
        } catch (IllegalArgumentException e) {
            System.out.println("expected " + e.getMessage());
        }
        try {
            switchThread(false);
            calc.setPrincipal(12);
            calc.setPrincipal(1);
            fail("must not allow operation outside the main tread");
        } catch (IllegalStateException e) {
            System.out.println("expected");
        }
        switchThread(true);
        calc.setPrincipal(123);
    }

    @Test
    public void setRate() throws Exception {
        InterestCalculator calc = new BaseInterestRateCalculator();
        try {
            calc.setRate(-1.89);
            calc.setRate(-100);
            calc.setRate(-0.001);
            calc.setRate(-0.1);
            fail("must not accept negative values");
        } catch (IllegalArgumentException e) {
            System.out.println("expected " + e.getMessage());
        }
        try {
            switchThread(false);
            calc.setRate(12);
            calc.setRate(1);
            fail("must not allow operation outside the main tread");
        } catch (IllegalStateException e) {
            System.out.println("expected");
        }
        switchThread(true);
        calc.setRate(123);
    }

    @Test
    public void setDuration() throws Exception {
        InterestCalculator calc = new BaseInterestRateCalculator();
        try {
            calc.setDuration(-1);
            calc.setDuration(-100);
            calc.setDuration(-0.001);
            calc.setDuration(-0.1);
            fail("must not accept negative values");
        } catch (IllegalArgumentException e) {
            System.out.println("expected " + e.getMessage());
        }
        try {
            switchThread(false);
            calc.setDuration(12);
            calc.setDuration(1);
            fail("must not allow operation outside the main tread");
        } catch (IllegalStateException e) {
            System.out.println("expected");
        }
        switchThread(true);
        calc.setDuration(123);
    }

    @Test
    public void getDuration() throws Exception {
        BaseInterestRateCalculator calc = new BaseInterestRateCalculator();
        assertNotNull(calc.getDuration());
        try {
            switchThread(false);
            calc.getDuration();
            fail("must not allow operation outside the main thread");
        } catch (IllegalStateException e) {
            System.out.println("expected");
        }
        switchThread(true);
        calc.getDuration(); //must not throw now
    }


    private void switchThread(boolean isMainThread) {
        when(mockLooper.getThread()).thenReturn(isMainThread
                ? Thread.currentThread() : mainThreadMock);
    }
}