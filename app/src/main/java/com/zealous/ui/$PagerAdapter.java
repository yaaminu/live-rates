package com.zealous.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.zealous.R;
import com.zealous.news.NewsFragment;
import com.zealous.utils.GenericUtils;

/**
 * Created by yaaminu on 4/28/17.
 */
public class $PagerAdapter extends FragmentPagerAdapter {

    private final BaseFragment[] fragments;
    private final String[] titles;

    public $PagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        fragments = new BaseFragment[]{
                NewsFragment.create(false),
                NewsFragment.create(true)
        };
        titles = new String[]{
                GenericUtils.getString(R.string.home),
                GenericUtils.getString(R.string.favorites)
        };
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
