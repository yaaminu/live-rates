package com.zealous.exchangeRates;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.zealous.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

public class ExchangeRateListActivity extends SearchActivity {

    public static final String EXTRA_PICK_CURRENCY = "pick_currrency";
    public static final String EXTRA_SELECTED = "selected";
    public static final String SEARCH = "search";
    public static final String EVENT_RATE_SELECTED = "event_rate_selected";

    @Inject
    EventBus bus;

    @Override
    protected int getLayout() {
        return R.layout.activity_exchange_rates;
    }

    @Override
    protected boolean hasParent() {
        return true;
    }

    @Override
    protected void doCreate(@Nullable Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        DaggerExchangeRateListActivityComponent.create()
                .inject(this);
        bus.register(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new ExchangeRateFragment(), "exchangeRates")
                .commit();
    }

    @Override
    protected void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }

    @Override
    protected void doSearch(String constraint) {
        bus.post(Collections.singletonMap(SEARCH, constraint));
    }

    @Subscribe
    public void onEvent(Object event) {
        if (event instanceof Map && ((Map) event).containsKey(EVENT_RATE_SELECTED)) {

            @SuppressWarnings("unchecked")
            Map<String, ExchangeRate> tmp = (Map<String, ExchangeRate>) event;

            Intent results = new Intent();
            Bundle bundle = new Bundle(1);
            bundle.putString(EXTRA_SELECTED, tmp.get(EVENT_RATE_SELECTED).getCurrencyIso());
            results.putExtras(bundle);
            setResult(RESULT_OK, results);
            finish();
        }
    }
}
