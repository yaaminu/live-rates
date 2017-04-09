package com.zealous.expense;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yaaminu on 4/9/17.
 */

@Singleton
@Module
public class ExpenseActivityProvider {
    @Provides
    public ExpenseFragment createExpenseFragment() {
        return new ExpenseFragment();
    }
}
