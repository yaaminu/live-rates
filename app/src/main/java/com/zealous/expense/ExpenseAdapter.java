package com.zealous.expense;

import android.text.format.DateUtils;
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
        final Expenditure item = getItem(position);
        holder.expeditureDescription.setText(item.getDescription());
        holder.expenditureTime.setText(DateUtils.getRelativeDateTimeString(delegate.context(),
                item.getExpenditureTime().getTime(), System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));
        holder.expenseLocation.setText(String.valueOf(" " + item.getLocation()));
        holder.categoryIcon.setImageResource(item.getCategory().getIcon(delegate.context()));
        holder.expenditureAmount.setText(delegate.context().getString(R.string.amount_format, item.getNormalizedAmount()));
    }

    @Override
    public ExpenseItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ExpenseItemHolder(LayoutInflater
                .from(parent.getContext()).inflate(R.layout.expense_list_item, parent, false));
    }

    interface Delegate extends BaseAdapter.Delegate<ExpenseItemHolder, Expenditure> {
    }
}
