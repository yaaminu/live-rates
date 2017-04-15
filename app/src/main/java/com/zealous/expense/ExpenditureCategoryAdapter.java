package com.zealous.expense;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
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
    protected void doBindHolder(CategoryHolder holder, int position) {
        final ExpenditureCategory item = getItem(position);
        holder.name.setText(item.getName());
        final Drawable drawable = delegate.context().getResources().getDrawable(item.getIcon(delegate.context()));
        DrawableCompat.setTint(drawable, ContextCompat.getColor(delegate.context(), R.color.light_violet));
        holder.categoryIcon.setImageDrawable(drawable);
        holder.itemView.setSelected(position == delegate.getSelectedItemPosition());
    }

    @Override
    public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryHolder(inflater.inflate(R.layout.category_list_item, parent, false));
    }

    public interface Delegate extends BaseAdapter.Delegate<CategoryHolder, ExpenditureCategory> {
        int getSelectedItemPosition();
    }
}
