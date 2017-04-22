package com.zealous.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.zealous.R;

public class BudgetActivity extends BaseZealousActivity {

    @Override
    protected int getLayout() {
        return R.layout.activity_budget;
    }

    @Override
    protected boolean hasParent() {
        return true;
    }

    @Override
    protected void doCreate(@Nullable Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
    }
}
