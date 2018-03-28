package com.zealous.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.zealous.R;

import butterknife.BindView;


/**
 * Created by yaaminu on 4/28/17.
 */
public class GSEFragmentParent extends BaseFragment {


    @Override
    protected int getLayout() {
        return R.layout.gse_fragment_parent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
