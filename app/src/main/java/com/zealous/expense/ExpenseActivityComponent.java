package com.zealous.expense;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yaaminu on 4/9/17.
 */

@Singleton
@Component(modules = {ExpenseActivityProvider.class})
public interface ExpenseActivityComponent {
    void inject(ExpenseActivity expenseActivity);
}
