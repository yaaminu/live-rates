package com.zealous.expense;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yaaminu on 4/17/17.
 */

@Module
@Singleton
public class MainActivityEventBusProvider {
    private static final EventBus EVENT_BUS = EventBus.builder().build();

    @Provides
    @Singleton
    public EventBus getTheBus() {
        return EVENT_BUS;
    }
}
