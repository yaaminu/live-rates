package com.zealous.exchangeRates;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.view.View;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.ViewUtils;

import java.util.Collections;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.zealous.utils.GenericUtils.getString;

/**
 * Created by yaaminu on 4/14/17.
 */

public class ExchangeRateAdapterDelegateImpl implements ExchangeRatesListAdapter.Delegate {
    private final ExchangeRate baseRate;
    private final ExchangeRateFragment fragment;
    final RealmChangeListener<RealmResults<ExchangeRate>> changeListener = new RealmChangeListener<RealmResults<ExchangeRate>>() {
        @Override
        public void onChange(RealmResults<ExchangeRate> element) {
            fragment.adapter.notifyDataChanged(fragment.filter);
            long lastUpdated = ExchangeRateManager.lastUpdated();
            long now = System.currentTimeMillis();
            if (now - lastUpdated < 60 * 1000) {
                fragment.tvLastUpdated.setText(getString(R.string.last_updated_s, getString(R.string.now)));
            } else {
                fragment.tvLastUpdated.setText(
                        getString(R.string.last_updated_s, (lastUpdated <= 0 ? getString(R.string.last_updated_never) :
                                DateUtils.getRelativeTimeSpanString(lastUpdated, now, DateUtils.MINUTE_IN_MILLIS))));
            }
        }
    };
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
        exchangeRates.addChangeListener(changeListener);

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
            com.zealous.utils.ViewUtils.showViews(fragment.emptyView);
            ViewUtils.hideViews(fragment.recyclerView);
        } else {
            ViewUtils.hideViews(fragment.emptyView);
            ViewUtils.showViews(fragment.recyclerView);
        }
        return ret;
    }
}
