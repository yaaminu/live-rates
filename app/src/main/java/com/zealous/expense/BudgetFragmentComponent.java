package com.zealous.expense;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yaaminu on 4/17/17.
 */

@Singleton
@Component(modules = {BudgetFragmentProvider.class, BaseExpenditureProvider.class})
public interface BudgetFragmentComponent {
    void inject(BudgetFragment fragment);
}
