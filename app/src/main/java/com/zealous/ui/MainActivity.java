package com.zealous.ui;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;

import com.zealous.R;
import com.zealous.exchangeRates.ExchangeRateManager;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends BaseZealousActivity {
    @Bind(R.id.pager)
    ViewPager pager;
    int currentItem;

    private String[] titles;

    @ColorRes
    static final int[] colors = {
            R.color.expenseColorPrimary,
            R.color.exchangeRatePrimary,
            R.color.bankRatesPrimary,
            R.color.otherRatesPrimary,
            R.color.calculatorsPrimary

    };
    @ColorRes
    static final int[] darkColors = {
            R.color.expenseColorPrimaryDark,
            R.color.exchangeRatePrimaryDark,
            R.color.bankRatesPrimaryDark,
            R.color.otherRatesPrimaryDark,
            R.color.calculatorsPrimaryDark

    };
    private Subscription timerSubscription;
    private Action1<Long> subscriber = new Action1<Long>() {
        @Override
        public void call(Long aLong) {
            currentItem = currentItem >= homePagerAdapter.getCount() ? 0 : currentItem;
            pager.setCurrentItem(currentItem);
            paintToolbar();
            currentItem++;
        }
    };

    private void paintToolbar() {
        int backgroundColor = ContextCompat.getColor(MainActivity.this, colors[currentItem]);
        assert toolbar != null;
        toolbar.setBackgroundColor(backgroundColor);
        toolbar.setTitle(titles[currentItem]);
        setUpStatusBarColor(darkColors[currentItem]);
    }

    private HomePagerAdapter homePagerAdapter;
    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            currentItem = position;
            paintToolbar();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void doCreate(@Nullable Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        ExchangeRateManager.loadRates();
        assert toolbar != null;
        toolbar.setNavigationIcon(R.drawable.ic_home_black_24dp);
        titles = getResources().getStringArray(R.array.home_menu_titles);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(titles[0]);
        homePagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(homePagerAdapter);
        pager.addOnPageChangeListener(pageChangeListener);
        currentItem = 0;

    }

    @Override
    protected void onResume() {
        super.onResume();
        timerSubscription = rx.Observable.interval(3, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    @Override
    protected void onPause() {
        if (!timerSubscription.isUnsubscribed()) {
            timerSubscription.unsubscribe();
        }
        timerSubscription = null;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        pager.removeOnPageChangeListener(pageChangeListener);
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
