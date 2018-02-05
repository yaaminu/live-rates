package com.zealous.expense;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yaaminu on 4/8/17.
 */
public class ExpenseItemHolder extends BaseAdapter.Holder {
    private final ExpenseAdapter.Delegate delegate;
    @BindView(R.id.expense_description)
    TextView expeditureDescription;
    @BindView(R.id.tv_location)
    TextView expenseLocation;
    @BindView(R.id.category_icon)
    ImageView categoryIcon;
    @BindView(R.id.tv_expense_amount)
    TextView expenditureAmount;
    @BindView(R.id.expenditure_time)
    TextView expenditureTime;
    @BindView(R.id.option_view)
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
