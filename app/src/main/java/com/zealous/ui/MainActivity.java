package com.zealous.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.zealous.R;
import com.zealous.exchangeRates.DaggerMainActivityComponent;
import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.exchangeRates.ExchangeRateDetailActivity;
import com.zealous.exchangeRates.ExchangeRateFragment;
import com.zealous.exchangeRates.SearchActivity;
import com.zealous.expense.ExpenseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import dagger.Lazy;

import static com.zealous.exchangeRates.ExchangeRateListActivity.EVENT_RATE_SELECTED;
import static com.zealous.exchangeRates.ExchangeRateListActivity.SEARCH;

public class MainActivity extends SearchActivity {

    @Bind(R.id.bottomBar)
    BottomBar bottomBar;

    @Inject
    EventBus bus;

    @Inject
    Lazy<ExchangeRateFragment> exchangeRateFragmentLazy;
    @Inject
    Lazy<ExpenseFragment> expenseFragmentLazy;
    @Inject
    Lazy<ToolsFragment> toolsFragmentLazy;
    @Inject
    Lazy<BusinessNewsFragmentParent> businessNewsFragmentLazy;
    private Fragment previousFragment;

    @Override
    protected void doCreate(@Nullable Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        DaggerMainActivityComponent.create()
                .inject(this);
        bus.register(this);
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
                updateToolbar(tabId);
                supportInvalidateOptionsMenu();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void updateToolbar(@IdRes int id) {
        final ActionBar supportActionBar = getSupportActionBar();
        if (id == R.id.tab_expenses) {
            if (supportActionBar != null) {
                supportActionBar.hide();
            }
        } else {
            if (toolbar != null) {
                if (supportActionBar != null) {
                    supportActionBar.show();
                }
                closeSearch();
                toolbar.setBackgroundColor(ContextCompat.getColor(this, getToolBarColor(id)));
            }
            setUpStatusBarColor(getStatusBarColor(id));
        }
    }

    @ColorRes
    private int getStatusBarColor(int id) {
        switch (id) {
            case R.id.tab_exchange_rates:
                return R.color.exchangeRatePrimaryDark;
            case R.id.tab_business_news:
                return R.color.business_news_color_primary_dark;
            case R.id.tab_expenses:
                return R.color.dark_violet;
            case R.id.tab_tools:
                return R.color.calculatorsPrimaryDark;
            default:
                throw new AssertionError();
        }
    }

    @ColorRes
    private int getToolBarColor(int id) {
        switch (id) {
            case R.id.tab_exchange_rates:
                return R.color.exchangeRatePrimary;
            case R.id.tab_business_news:
                return R.color.business_news_color_primary;
            case R.id.tab_expenses:
                return R.color.light_violet;
            case R.id.tab_tools:
                return R.color.calculatorsPrimary;
            default:
                throw new AssertionError();
        }
    }

    @Override
    protected boolean showSearch() {
        switch (bottomBar.getCurrentTabId()) {
            case R.id.tab_exchange_rates:
            case R.id.tab_business_news:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void doSearch(String constraint) {
        bus.post(Collections.singletonMap(SEARCH, constraint));
    }

    private Fragment getFragment(int tabId) {
        switch (tabId) {
            case R.id.tab_exchange_rates:
                return exchangeRateFragmentLazy.get();
            case R.id.tab_expenses:
                return expenseFragmentLazy.get();
            case R.id.tab_tools:
                return toolsFragmentLazy.get();
            case R.id.tab_business_news:
                return businessNewsFragmentLazy.get();
            default:
                throw new AssertionError();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        if (event instanceof Map) {
            if (((Map) event).containsKey(EVENT_RATE_SELECTED)) {
                ExchangeRate exchangeRate = ((ExchangeRate) ((Map) event).get(EVENT_RATE_SELECTED));
                Intent intent = new Intent(this, ExchangeRateDetailActivity.class);
                intent.putExtra(ExchangeRateDetailActivity.EXTRA_CURRENCY_SOURCE, "GHS");
                intent.putExtra(ExchangeRateDetailActivity.EXTRA_START_WITH, exchangeRate.getRate() >= 1 ? "GHS" : exchangeRate.getCurrencyIso());
                intent.putExtra(ExchangeRateDetailActivity.EXTRA_CURRENCY_TARGET, exchangeRate.getCurrencyIso());
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        bus.unregister(this);
        super.onDestroy();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected boolean hasParent() {
        return false;
    }
}
