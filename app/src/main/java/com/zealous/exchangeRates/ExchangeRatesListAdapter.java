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

import butterknife.BindView;


/**
 * @author by yaaminu on 1/3/17.
 */
public class ExchangeRatesListAdapter extends BaseAdapter<ExchangeRatesListAdapter.Holder, ExchangeRate> {


    private final Delegate delegate;

    public ExchangeRatesListAdapter(Delegate delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    protected void doBindHolder(Holder holder, int position) {
        ExchangeRate rate = getItem(position);
        holder.currencyIcon.setImageResource(rate.getCurrencyIcon(delegate.context()));
        holder.currencyName.setText(rate.getCurrencyName());
        holder.currencyRate.setText(getText(rate));
    }

    private String getText(ExchangeRate rate) {
        Context context = delegate.context();
        if (rate.getRate() == 0) return "??";
        if (rate.getRate() > 1) {
            return context.getString(R.string.rate_template, rate.getCurrencySymbol(), ExchangeRate.FORMAT.format(rate.getRate()), delegate.baseRate().getCurrencySymbol(), ExchangeRate.FORMAT.format(1));
        }

        return context.getString(R.string.rate_template, rate.getCurrencySymbol(), ExchangeRate.FORMAT.format(1),
                delegate.baseRate().getCurrencySymbol(), ExchangeRate.FORMAT.format(BigDecimal.ONE.divide(BigDecimal.valueOf(rate.getRate()),
                        MathContext.DECIMAL128)));
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.exchange_rate_list_item, parent, false));
    }

    public interface Delegate extends BaseAdapter.Delegate<Holder, ExchangeRate> {
        ExchangeRate baseRate();
    }

    public static class Holder extends BaseAdapter.Holder {
        @BindView(R.id.iv_currency_icon)
        ImageView currencyIcon;
        @BindView(R.id.tv_currency_name)
        TextView currencyName;
        @BindView(R.id.tv_rate)
        TextView currencyRate;

        public Holder(View v) {
            super(v);
        }
    }
}
