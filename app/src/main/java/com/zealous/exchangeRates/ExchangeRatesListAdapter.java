package com.zealous.exchangeRates;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;

import butterknife.Bind;

/**
 * @author by yaaminu on 1/3/17.
 */
public class ExchangeRatesListAdapter extends BaseAdapter<ExchangeRatesListAdapter.Holder, ExchangeRate> {


    private final String packageName;
    private final Delegate delegate;

    public ExchangeRatesListAdapter(Delegate delegate) {
        super(delegate);
        packageName = delegate.context().getPackageName();
        this.delegate = delegate;
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
        holder.currencyRate.setText(getText(rate));
    }

    private String getText(ExchangeRate rate) {
        Context context = delegate.context();
        if (rate.getRate() > 1) {
            return context.getString(R.string.rate_template, delegate.baseRate().getCurrencySymbol(), ExchangeRate.FORMAT.format(1),
                    rate.getCurrencySymbol(), ExchangeRate.FORMAT.format(rate.getRate()));
        }

        return context.getString(R.string.rate_template, delegate.baseRate().getCurrencySymbol(), ExchangeRate.FORMAT.format(BigDecimal.ONE.divide(BigDecimal.valueOf(rate.getRate()),
                MathContext.DECIMAL128)), rate.getCurrencySymbol(), ExchangeRate.FORMAT.format(1));
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.exchange_rate_list_item, parent, false));
    }

    public interface Delegate extends BaseAdapter.Delegate<Holder, ExchangeRate> {
        ExchangeRate baseRate();
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
}
