package com.zealous.exchangeRates;

import com.zealous.expense.MainActivityEventBusProvider;
import com.zealous.ui.MainActivity;
import com.zealous.ui.MainActivityFragmentsProvider;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yaaminu on 4/17/17.
 */

@Singleton
@Component(modules = {MainActivityEventBusProvider.class, MainActivityFragmentsProvider.class})
public interface MainActivityComponent {
    void inject(MainActivity activity);
}
