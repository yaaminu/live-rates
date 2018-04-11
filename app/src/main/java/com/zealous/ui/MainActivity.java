package com.zealous.ui;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.zealous.R;
import com.zealous.equity.EquityListFragment;
import com.zealous.equity.ExchangeRateFragmentParent;
import com.zealous.exchangeRates.DaggerMainActivityComponent;
import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.exchangeRates.ExchangeRateDetailActivity;
import com.zealous.exchangeRates.SearchActivity;
import com.zealous.expense.InsetRateCalculator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import dagger.Lazy;

import static com.zealous.exchangeRates.ExchangeRateListActivity.EVENT_RATE_SELECTED;
import static com.zealous.exchangeRates.ExchangeRateListActivity.SEARCH;

public class MainActivity extends SearchActivity {

    @BindView(R.id.bottomBar)
    BottomBar bottomBar;


    @Inject
    EventBus bus;

    @Inject
    Lazy<ExchangeRateFragmentParent> exchangeRateFragmentLazy;

    @Inject
    Lazy<HomeFragmentParent> homeFragmentLazy;

    @Inject
    Lazy<EquityListFragment> equityFragmentParentLazy;

    private Fragment previousFragment;

    @Override
    protected void doCreate(@Nullable Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        DaggerMainActivityComponent.create()
                .inject(this);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                Fragment tmp = getFragment(tabId);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.container, tmp, String.valueOf(tabId))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commit();
                transaction = getSupportFragmentManager()
                        .beginTransaction();

                if (previousFragment != null) {
                    transaction.remove(previousFragment);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    transaction.commit();
                }
                previousFragment = tmp;
                supportInvalidateOptionsMenu();
                if (toolbar != null) {
                    toolbar.setTitle(tabId == R.id.tab_home ? getString(R.string.app_name) : bottomBar.getTabWithId(tabId).getTitle());
                }
            }
        });
    }

    private Fragment getFragment(int tabId) {
        switch (tabId) {
            case R.id.tab_home:
                return homeFragmentLazy.get();
            case R.id.tab_exchange_rates:
                return exchangeRateFragmentLazy.get();
            case R.id.tab_gse:
                return equityFragmentParentLazy.get();
            default:
                throw new AssertionError();
        }
    }


    @Override
    protected boolean showSearch() {
        switch (bottomBar.getCurrentTabId()) {
            case R.id.tab_exchange_rates:
            case R.id.tab_gse:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void doSearch(String constraint) {
        bus.post(Collections.singletonMap(SEARCH, constraint));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        if (event instanceof Map) {
            if (((Map) event).containsKey(EVENT_RATE_SELECTED)) {
                ExchangeRate exchangeRate = ((ExchangeRate) ((Map) event).get(EVENT_RATE_SELECTED));
                Bundle intent = new Bundle(3);
                intent.putString(ExchangeRateDetailActivity.EXTRA_CURRENCY_SOURCE, "GHS");
                intent.putString(ExchangeRateDetailActivity.EXTRA_START_WITH, exchangeRate.getRate() >= 1 ? "GHS" : exchangeRate.getCurrencyIso());
                intent.putString(ExchangeRateDetailActivity.EXTRA_CURRENCY_TARGET, exchangeRate.getCurrencyIso());
                InsetRateCalculator fragment = new InsetRateCalculator();
                fragment.setArguments(intent);
                fragment.show(getSupportFragmentManager(), exchangeRate.getCurrencyIso());
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomBar.getCurrentTabId() == R.id.tab_home)
            super.onBackPressed();
        else
            bottomBar.selectTabAtPosition(0,true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    protected void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Override
    protected boolean hasParent() {
        return false;
    }
}
