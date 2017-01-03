package com.zealous.bankRates;

import com.zealous.adapter.SimpleListItemHolder;
import com.zealous.adapter.SimpleRecyclerViewAdapter;
import com.zealous.adapter.Tuple;

/**
 * Created by yaaminu on 1/3/17.
 */
public class BankRatesRecyclerViewAdapter extends SimpleRecyclerViewAdapter<Tuple> {
    public BankRatesRecyclerViewAdapter(Delegate<Tuple> delegate) {
        super(delegate);
    }

    @Override
    protected void doBindHolder(SimpleListItemHolder holder, int position) {
        Tuple item = getItem(position);
        holder.first.setText(item.getFirst());
        holder.second.setText(item.getSecond());
    }
}
