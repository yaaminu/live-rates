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

import javax.inject.Inject;

import butterknife.BindView;
import dagger.Lazy;

public class MainActivity extends BaseZealousActivity {

    @BindView(R.id.bottomBar)
    BottomBar bottomBar;

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
    protected void onDestroy() {
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
