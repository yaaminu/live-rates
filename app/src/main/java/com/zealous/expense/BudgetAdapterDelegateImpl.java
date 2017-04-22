package com.zealous.expense;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.zealous.adapter.BaseAdapter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by yaaminu on 4/17/17.
 */

public class BudgetAdapterDelegateImpl implements BudgetAdapter.Delegate {

    private final BudgetFragment fragment;

    public BudgetAdapterDelegateImpl(BudgetFragment fragment) {
        this.fragment = fragment;
    }


    @Override
    public BigDecimal getTotalExpenditureForCategory(ExpenditureCategory category) {
        return fragment.getTotalExpenditure(category);
    }

    @Override
    public String[] durationTypes() {
        return fragment.getDurationTypes();
    }

    @Override
    public Context context() {
        return fragment.getContext();
    }

    @Override
    public void onItemClick(BaseAdapter<BudgetViewHolder, ExpenditureCategory> adapter, View view, int position, long id) {

    }

    @Override
    public boolean onItemLongClick(BaseAdapter<BudgetViewHolder, ExpenditureCategory> adapter, View view, int position, long id) {
        return false;
    }

    @NonNull
    @Override
    public List<ExpenditureCategory> dataSet(String constrain) {
        return fragment.dataSet();
    }
}
