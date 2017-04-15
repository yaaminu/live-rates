package com.zealous.expense;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yaaminu on 4/14/17.
 */

@Singleton
@Component(modules = {AddExpenseFragmentProvider.class})
public interface AddExpenditureComponent {
    void inject(AddExpenseFragment fragment);
}
