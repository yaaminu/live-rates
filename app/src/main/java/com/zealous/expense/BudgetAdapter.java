package com.zealous.expense;

import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;
import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.utils.ViewUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static com.zealous.exchangeRates.ExchangeRate.FORMAT;

/**
 * @author by yaaminu on 4/17/17.
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
        final BigDecimal expenditure = delegate.getTotalExpenditureForCategory(item),
                budget = BigDecimal.valueOf(item.getBudget()).divide(BigDecimal.valueOf(100), MathContext.DECIMAL128);

        holder.categoryName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        holder.categoryName.setCompoundDrawablesWithIntrinsicBounds(item.getIconViolet(delegate.context()), 0, 0, 0);
        holder.categoryName.setText(String.valueOf(" " + item.getName()));
        holder.budget.setText(delegate.context().getString(R.string.budget_amount_template, item.getNormalizedBudget(),
                delegate.durationTypes()[item.getBudgetDuration()]));
        holder.expenditure.setText(delegate.context().getString(R.string.spent, FORMAT.format(expenditure)));
        if (expenditure.compareTo(budget) == 1) { //over spending
            holder.budgetDeficit.setTextColor(ContextCompat.getColor(
                    delegate.context(), R.color.business_news_color_primary));
            holder.budgetDeficit.setText(delegate.context().getString(R.string.budget_deficit,
                    FORMAT.format(Math.abs(budget.subtract(expenditure).doubleValue()))));
        } else {
            holder.budgetDeficit.setTextColor(ContextCompat.getColor(
                    delegate.context(), R.color.lemon));
            holder.budgetDeficit.setText(delegate.context().getString(R.string.left, FORMAT.format(budget.subtract(expenditure).doubleValue())));
        }
        if (expenditure.doubleValue() == 0 || budget.doubleValue() == 0) {
            holder.expenseMeter.setProgress(0);
            ViewUtils.hideViews(holder.expenseMeter);
        } else {
            holder.expenseMeter.setProgress(Math.min(expenditure.divide(budget, MathContext.DECIMAL128).multiply(BigDecimal.valueOf(100))
                    .round(new MathContext(0, RoundingMode.HALF_DOWN)).intValue(), 100));
        }
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
