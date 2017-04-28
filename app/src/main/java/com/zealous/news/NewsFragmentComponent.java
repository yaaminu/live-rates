package com.zealous.news;

import com.zealous.expense.MainActivityEventBusProvider;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yaaminu on 4/25/17.
 */

@Singleton
@Component(modules = {NewsFragmentProvider.class, PicasoProvider.class, BaseNewsProvider.class, MainActivityEventBusProvider.class})
public interface NewsFragmentComponent {
    void inject(NewsFragment newsFragment);
}
