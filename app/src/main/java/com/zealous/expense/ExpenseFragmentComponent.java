package com.zealous.expense;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yaaminu on 4/8/17.
 */

@Singleton
@Component(modules = {
        BaseExpenditureProvider.class,
        ExpenseFragmentProvider.class,
        MainActivityEventBusProvider.class
})
public interface ExpenseFragmentComponent {
    void inject(ExpenseFragment expenditureComponent);
}
