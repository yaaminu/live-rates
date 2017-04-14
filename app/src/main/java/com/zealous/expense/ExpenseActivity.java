package com.zealous.expense;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.zealous.R;
import com.zealous.Zealous;
import com.zealous.ui.BaseZealousActivity;

import javax.inject.Inject;

import butterknife.OnClick;

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

    @OnClick({R.id.back, R.id.options})
    void handleClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            default:
                throw new RuntimeException("unknown ID");
        }
    }

}
