package com.zealous.expense;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.zealous.R;
import com.zealous.Zealous;
import com.zealous.ui.BaseZealousActivity;

import javax.inject.Inject;

public class ExpenseActivity extends BaseZealousActivity {

    @Inject
    ExpenseFragment expenseFragment;

    @Override
    protected int getLayout() {
        return R.layout.activity_expense;
    }

    @Override
    protected boolean hasParent() {
        return true;
    }

    @Override
    protected void doCreate(@Nullable Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        ((Zealous) getApplication()).getExpenseActivityComponent()
                .inject(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.expense_fragment_container, expenseFragment)
                .commit();
    }
}
