package com.zealous.expense;

import android.view.ViewGroup;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

/**
 * Created by yaaminu on 4/14/17.
 */
public class ExpenditureCategoryAdapter extends BaseAdapter<CategoryHolder, ExpenditureCategory> {
    private final Delegate delegate;

    public ExpenditureCategoryAdapter(Delegate delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    protected void doBindHolder(CategoryHolder holder, int position) {
        ((TextView) holder.itemView).setText(getItem(position).getName());
        ((TextView) holder.itemView).setTextColor(delegate.context().getResources()
                .getColor(delegate.getSelectedItemPosition() == position ?
                        R.color.business_news_color_primary : R.color.black));
    }

    @Override
    public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryHolder(inflater.inflate(android.R.layout.simple_list_item_1, parent, false));
    }

    public interface Delegate extends BaseAdapter.Delegate<CategoryHolder, ExpenditureCategory> {
        int getSelectedItemPosition();
    }
}
