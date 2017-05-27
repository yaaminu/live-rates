package com.zealous.exchangeRates;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;

/**
 * Created by yaaminu on 5/27/17.
 */
@Module
public class ExchangeRateFragmentModule {

    private final ExchangeRateFragment exchangeRateFragment;

    public ExchangeRateFragmentModule(ExchangeRateFragment exchangeRateFragment) {
        this.exchangeRateFragment = exchangeRateFragment;
    }

    @Singleton
    @Provides
    public Realm getRealm() {
        return ExchangeRate.Realm(exchangeRateFragment.getContext());
    }

    @Singleton
    @Provides
    public ExchangeRatesListAdapter.Delegate getDelegate(Realm realm) {
        return new ExchangeRateAdapterDelegateImpl(exchangeRateFragment, realm);
    }

    @Singleton
    @Provides
    public ExchangeRatesListAdapter getAdapter(ExchangeRatesListAdapter.Delegate delegate) {
        return new ExchangeRatesListAdapter(delegate);
    }

    @Singleton
    @Provides
    public ExchangeRateManager getRatesManager(EventBus bus) {
        return new ExchangeRateManager(bus);
    }
}
