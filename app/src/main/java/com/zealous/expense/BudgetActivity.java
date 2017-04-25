package com.zealous.expense;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.zealous.R;
import com.zealous.ui.BaseZealousActivity;

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
