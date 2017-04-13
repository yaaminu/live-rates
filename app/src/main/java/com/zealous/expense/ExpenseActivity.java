package com.zealous.expense;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;

import com.zealous.R;
import com.zealous.Zealous;
import com.zealous.ui.BaseZealousActivity;

import javax.inject.Inject;

import butterknife.OnClick;

public class ExpenseActivity extends BaseZealousActivity {

    @Inject
    ExpenseFragment expenseFragment;

    private PopupMenu.OnMenuItemClickListener menuClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return expenseFragment.onOptionsItemSelected(item);
        }
    };

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
            case R.id.options:
                showOptionsMenu(v);
                break;
            case R.id.back:
                onBackPressed();
                break;
            default:
                throw new RuntimeException("unknown ID");
        }
    }

    void showOptionsMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.inflate(R.menu.expense_menu);
        popupMenu.setOnMenuItemClickListener(menuClickListener);
        popupMenu.show();
    }
}
