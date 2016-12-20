package com.zealous.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.zealous.bankRates.BankRatesFragment;
import com.zealous.exchangeRates.ExchageRatesHome;
import com.zealous.expense.ExpenseTrackerHome;

/**
 * Created by yaaminu on 12/20/16.
 */
public class HomePagerAdapter extends FragmentPagerAdapter {

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        BaseFragment fragment;
        switch (position) {
            case 0:
                fragment = new ExpenseTrackerHome();
                break;
            case 1:
                fragment = new ExchageRatesHome();
                break;
            case 2:
                fragment = new BankRatesFragment();
                break;
            default:
                fragment = new HomeMenuItemsFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
