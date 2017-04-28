package com.zealous.ui;

import com.zealous.exchangeRates.ExchangeRateFragment;
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
    public ExchangeRateFragment getExchangeRateFragment() {
        return new ExchangeRateFragment();
    }

    @Provides
    public ToolsFragment toolsFragment() {
        return new ToolsFragment();
    }

    @Provides
    public BusinessNewsFragmentParent businessNewsFragment() {
        return new BusinessNewsFragmentParent();
    }
}
