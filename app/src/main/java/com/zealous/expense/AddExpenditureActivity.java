package com.zealous.expense;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.zealous.R;
import com.zealous.ui.BaseZealousActivity;

/**
 * Created by yaaminu on 4/14/17.
 */

public class AddExpenditureActivity extends BaseZealousActivity {

    @Override
    protected void doCreate(@Nullable Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        setUpStatusBarColor(R.color.dark_violet);
        assert toolbar != null;
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.light_violet));
    }

    @Override
    protected int getLayout() {
        return R.layout.actvity_add_expense;
    }

    @Override
    protected boolean hasParent() {
        return true;
    }
}
