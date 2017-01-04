package com.zealous.exchangeRates;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

import java.util.Locale;

import butterknife.Bind;

/**
 * Created by yaaminu on 1/3/17.
 */
public class ExchangeRatesListAdapter extends BaseAdapter<ExchangeRatesListAdapter.Holder, ExchangeRate> {


    private final String packageName;

    public ExchangeRatesListAdapter(Delegate delegate) {
        super(delegate);
        packageName = delegate.context().getPackageName();
    }

    @Override
    protected void doBindHolder(Holder holder, int position) {
        ExchangeRate rate = getItem(position);
        holder.currencyIcon.setImageResource(
                holder.getContext().getResources().getIdentifier("drawable/" +
                                rate.getCurrencyIso().toLowerCase(Locale.US),
                        null, packageName)
        );
        holder.currencyName.setText(rate.getCurrencyName());
        holder.currencyRate.setText(holder.getContext().getString(R.string.rate_template,
                rate.getCurrencySymbol(), "" + ExchangeRate.FORMAT.format(rate.getRate())));
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.exchange_rate_list_item, parent, false));
    }

    public static class Holder extends BaseAdapter.Holder {
        @Bind(R.id.iv_currency_icon)
        ImageView currencyIcon;
        @Bind(R.id.tv_currency_name)
        TextView currencyName;
        @Bind(R.id.tv_rate)
        TextView currencyRate;

        public Holder(View v) {
            super(v);
        }
    }

    public interface Delegate extends BaseAdapter.Delegate<Holder, ExchangeRate> {

    }
}
