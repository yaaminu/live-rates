package com.zealous.expense;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

/**
 * Created by yaaminu on 4/8/17.
 */

public class ExpenseAdapter extends BaseAdapter<ExpenseItemHolder, Expenditure> {

    public ExpenseAdapter(Delegate delegate) {
        super(delegate);
    }

    @Override
    protected void doBindHolder(ExpenseItemHolder holder, int position) {
        holder.expeditureDescription.setText(getItem(position).toString());
    }

    @Override
    public ExpenseItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ExpenseItemHolder(LayoutInflater
                .from(parent.getContext()).inflate(R.layout.expense_list_item, parent, false));
    }

    interface Delegate extends BaseAdapter.Delegate<ExpenseItemHolder, Expenditure> {
    }
}
