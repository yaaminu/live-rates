package com.zealous.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.zealous.R;

import butterknife.Bind;

/**
 * Created by yaaminu on 4/28/17.
 */
public class BusinessNewsFragmentParent extends BaseFragment {

    @Bind(R.id.pager)
    ViewPager pager;
    @Bind(R.id.tab_strip)
    TabLayout tabLayout;

    @Override
    protected int getLayout() {
        return R.layout.news_fragment_parent;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pager.setAdapter(new $PagerAdapter(getFragmentManager()));
        tabLayout.setupWithViewPager(pager);
    }
}
