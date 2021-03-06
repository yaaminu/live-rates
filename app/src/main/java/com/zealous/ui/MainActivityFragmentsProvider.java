package com.zealous.ui;

import com.zealous.equity.EquityListFragment;
import com.zealous.equity.ExchangeRateFragmentParent;
import com.zealous.expense.ExpenseFragment;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yaaminu on 4/17/17.
 */

@Module
public class MainActivityFragmentsProvider {
    @Provides
    public ExpenseFragment getExpenseFragment() {
        return new ExpenseFragment();
    }

    @Provides
    public ExchangeRateFragmentParent getExchangeRateFragment() {
        return new ExchangeRateFragmentParent();
    }

    @Provides
    public HomeFragmentParent homeFragmentParent() {
        return new HomeFragmentParent();
    }

    @Provides
    public EquityListFragment equityFragmentParent() {
        return new EquityListFragment();
    }
}
