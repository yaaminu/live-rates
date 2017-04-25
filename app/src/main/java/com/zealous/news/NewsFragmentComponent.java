package com.zealous.news;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yaaminu on 4/25/17.
 */

@Singleton @Component(modules = NewsFragmentProvider.class)
public interface NewsFragmentComponent {
    void inject(NewsFragment newsFragment);
}
