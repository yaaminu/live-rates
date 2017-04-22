package com.zealous.expense;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.zealous.R;
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
        fragment.update(adapter.getItem(position));
    }

    @Override
    public boolean onItemLongClick(BaseAdapter<BudgetViewHolder, ExpenditureCategory> adapter, View view, int position, long id) {

        final ExpenditureCategory category = adapter.getItem(position);
        new AlertDialog.Builder(fragment.getContext())
                .setItems(R.array.category_long_click_context_menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                fragment.update(category);
                                break;
                            case 1:
                                fragment.remove(category);
                                break;
                            default:
                                throw new AssertionError();
                        }
                    }
                }).create().show();
        return true;
    }

    @NonNull
    @Override
    public List<ExpenditureCategory> dataSet(String constrain) {
        return fragment.dataSet();
    }
}
