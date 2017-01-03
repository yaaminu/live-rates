package com.zealous.adapter;

import android.support.annotation.LayoutRes;
import android.view.ViewGroup;

import com.zealous.R;

/**
 * Created by yaaminu on 12/20/16.
 */

public abstract class SimpleRecyclerViewAdapter<T extends ITuple> extends BaseAdapter<SimpleListItemHolder, T> {

    public SimpleRecyclerViewAdapter(Delegate<T> delegate) {
        super(delegate);
    }


    @Override
    public SimpleListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = ((Delegate) delegate).getLayout();
        return new SimpleListItemHolder(inflater.inflate(layout == 0 ? R.layout.simple_list_item_2 : layout, parent, false));
    }

    public interface Delegate<T> extends BaseAdapter.Delegate<SimpleListItemHolder, T> {
        @LayoutRes
        int getLayout();
    }
}
