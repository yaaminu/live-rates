package com.zealous.expense;

import android.view.ViewGroup;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;
import com.zealous.exchangeRates.ExchangeRate;

import java.math.BigDecimal;

/**
 * Created by yaaminu on 4/17/17.
 */

public class BudgetAdapter extends BaseAdapter<BudgetViewHolder, ExpenditureCategory> {
    private final Delegate delegate;

    public BudgetAdapter(Delegate delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    protected void doBindHolder(BudgetViewHolder holder, int position) {
        final ExpenditureCategory item = getItem(position);
        final BigDecimal expenditure = delegate.getTotalExpenditureForCategory(item);
        holder.categoryName.setText(item.getName());
        holder.budget.setText(item.getNormalizedBudget());
        holder.expenditure.setText(ExchangeRate.FORMAT.format(expenditure));
        holder.budgetDeficit.setText(ExchangeRate.FORMAT.format(Math.min(BigDecimal.valueOf(item.getBudget()).subtract(expenditure).doubleValue(), 0)));
        holder.durationType.setText(delegate.durationTypes()[item.getBudgetDuration()]);
    }

    @Override
    public BudgetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BudgetViewHolder(inflater.inflate(R.layout.budget_list_item, parent, false));
    }

    public interface Delegate extends BaseAdapter.Delegate<BudgetViewHolder, ExpenditureCategory> {
        BigDecimal getTotalExpenditureForCategory(ExpenditureCategory category);

        String[] durationTypes();
    }
}
