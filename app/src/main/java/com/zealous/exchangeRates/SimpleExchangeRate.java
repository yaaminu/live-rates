package com.zealous.exchangeRates;

import com.zealous.adapter.SimpleListItemHolder;
import com.zealous.adapter.SimpleRecyclerViewAdapter;

import static com.zealous.exchangeRates.ExchangeRate.FORMAT;

/**
 * Created by yaaminu on 1/3/17.
 */
public class SimpleExchangeRate extends SimpleRecyclerViewAdapter<ExchangeRate> {
    public SimpleExchangeRate(SimpleRecyclerViewAdapter.Delegate<ExchangeRate> delegate) {
        super(delegate);
    }

    @Override
    protected void doBindHolder(SimpleListItemHolder holder, int position) {
        ExchangeRate item = getItem(position);
        holder.first.setText(item.getCurrencyName());
        holder.second.setText(item.getCurrencySymbol() + "  " + FORMAT.format(item.getRate()));
    }
}
