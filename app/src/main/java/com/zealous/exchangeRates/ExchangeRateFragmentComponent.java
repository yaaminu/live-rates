package com.zealous.exchangeRates;

import com.zealous.expense.MainActivityEventBusProvider;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yaaminu on 4/17/17.
 */

@Singleton
@Component(modules = {MainActivityEventBusProvider.class, ExchangeRateFragmentModule.class})
public interface ExchangeRateFragmentComponent {
    void inject(ExchangeRateFragment fragment);
}
