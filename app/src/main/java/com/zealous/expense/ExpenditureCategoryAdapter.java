package com.zealous.expense;

import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

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
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public ExpenditureCategory getItem(int position) {
        if (position == getItemCount() - 1) {
            return ExpenditureCategory.DUMMY_EXPENDITURE_CATEGORY;
        }
        return super.getItem(position);
    }

    @Override
    protected void doBindHolder(CategoryHolder holder, int position) {
        final ExpenditureCategory item = getItem(position);
        if (item == ExpenditureCategory.DUMMY_EXPENDITURE_CATEGORY) {
            holder.name.setText(R.string.add_new_category);
            holder.categoryIcon.setImageResource(R.drawable.ic_add_violet_24dp);
        } else {
            holder.name.setText(item.getName());
            final Drawable drawable = delegate.context().getResources().getDrawable(item.getIconViolet(delegate.context()));
            holder.categoryIcon.setImageDrawable(drawable);
            holder.itemView.setSelected(position == delegate.getSelectedItemPosition());
        }
    }

    @Override
    public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryHolder(inflater.inflate(R.layout.category_list_item, parent, false));
    }

    public interface Delegate extends BaseAdapter.Delegate<CategoryHolder, ExpenditureCategory> {
        int getSelectedItemPosition();
    }
}
