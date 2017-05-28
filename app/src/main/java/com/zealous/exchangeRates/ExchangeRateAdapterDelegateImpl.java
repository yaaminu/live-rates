package com.zealous.exchangeRates;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;
import com.zealous.utils.GenericUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.zealous.utils.ViewUtils.hideViews;
import static com.zealous.utils.ViewUtils.showViews;

/**
 * Created by yaaminu on 4/14/17.
 */

public class ExchangeRateAdapterDelegateImpl implements ExchangeRatesListAdapter.Delegate {
    private final ExchangeRate baseRate;
    private final ExchangeRateFragment fragment;

    private final RealmResults<ExchangeRate> exchangeRates;
    private final Realm realm;

    public ExchangeRateAdapterDelegateImpl(ExchangeRateFragment context,
                                           Realm realm) {
        this.realm = realm;
        this.fragment = context;
        this.baseRate = realm.where(ExchangeRate.class)
                .equalTo(ExchangeRate.FIELD_CURRENCY_ISO, "GHS")
                .findFirst();

        exchangeRates = realm.where(ExchangeRate.class).
                findAllSortedAsync(ExchangeRate.FIELD_WATCHING, Sort.DESCENDING,
                        ExchangeRate.FIELD_CURRENCY_NAME, Sort.ASCENDING);
        exchangeRates.addChangeListener(new RealmChangeListener<RealmResults<ExchangeRate>>() {
            @Override
            public void onChange(RealmResults<ExchangeRate> element) {
                fragment.adapter.notifyDataChanged("");
                exchangeRates.removeChangeListener(this);
            }
        });

    }

    @Override
    public ExchangeRate baseRate() {
        return baseRate;
    }

    @Override
    public Context context() {
        return fragment.getContext();
    }

    @Override
    public void onItemClick
            (BaseAdapter<ExchangeRatesListAdapter.Holder, ExchangeRate> adapter, View view,
             int position, long id) {
        final ExchangeRate exchangeRate = adapter.getItem(position);
        fragment.bus.post(Collections.singletonMap(ExchangeRateListActivity.EVENT_RATE_SELECTED, exchangeRate));
    }

    @Override
    public boolean onItemLongClick
            (BaseAdapter<ExchangeRatesListAdapter.Holder, ExchangeRate> adapter, View view,
             int position, long id) {

        return false;
    }

    @NonNull
    @Override
    public List<ExchangeRate> dataSet(String constraint) {
        List<ExchangeRate> ret;
        if (GenericUtils.isEmpty(constraint)) {
            ret = exchangeRates;
        } else {
            ret = realm.where(ExchangeRate.class).beginsWith(ExchangeRate.FIELD_CURRENCY_NAME, constraint, Case.INSENSITIVE)
                    .or()
                    .equalTo(ExchangeRate.FIELD_CURRENCY_ISO, constraint, Case.INSENSITIVE)
                    .findAllSorted(ExchangeRate.FIELD_WATCHING, Sort.DESCENDING,
                            ExchangeRate.FIELD_CURRENCY_NAME, Sort.ASCENDING);
        }
        if (ret.isEmpty()) {
            showViews(fragment.emptyView);
            hideViews(fragment.recyclerView, fragment.tvLastUpdated);
        } else {

            long lastUpdated = ExchangeRateManager.lastUpdated();
            if (lastUpdated == 0) {
                fragment.tvLastUpdated.setText(R.string.connect_get_rates_notice);
            } else if (System.currentTimeMillis() - lastUpdated
                    >= TimeUnit.DAYS.toMillis(2)) {
                showViews(fragment.tvLastUpdated);
                fragment.tvLastUpdated.setText(R.string.sale_rates_notice);
            } else {
                hideViews(fragment.tvLastUpdated);
            }
            hideViews(fragment.emptyView);
            showViews(fragment.recyclerView);
        }
        return ret;
    }
}
