package com.zealous.exchangeRates;

import com.zealous.expense.MainActivityEventBusProvider;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yaaminu on 4/17/17.
 */

@Singleton
@Component(modules = MainActivityEventBusProvider.class)
public interface ExchangeRateListActivityComponent {
    void inject(ExchangeRateListActivity activity);
}
