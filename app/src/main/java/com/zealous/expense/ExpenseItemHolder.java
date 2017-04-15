package com.zealous.expense;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by yaaminu on 4/8/17.
 */
public class ExpenseItemHolder extends BaseAdapter.Holder {
    private final ExpenseAdapter.Delegate delegate;
    @Bind(R.id.expense_description)
    TextView expeditureDescription;
    @Bind(R.id.tv_location)
    TextView expenseLocation;
    @Bind(R.id.category_icon)
    ImageView categoryIcon;
    @Bind(R.id.tv_expense_amount)
    TextView expenditureAmount;
    @Bind(R.id.expenditure_time)
    TextView expenditureTime;
    @Bind(R.id.option_view)
    View optionsView;

    public ExpenseItemHolder(View v, ExpenseAdapter.Delegate delegate) {
        super(v);
        this.delegate = delegate;
    }

    @OnClick({R.id.bt_edit, R.id.bt_delete})
    void onDeleteOrEdit(View v) {
        final int position = (int) itemView.getTag();
        switch (v.getId()) {
            case R.id.bt_edit:
                delegate.editItem(position);
                break;
            case R.id.bt_delete:
                delegate.deleteItem(position);
                break;
            default:
                throw new AssertionError();
        }
    }
}
