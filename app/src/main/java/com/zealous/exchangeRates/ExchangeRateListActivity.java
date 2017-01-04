package com.zealous.exchangeRates;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.ViewUtils;

import java.util.List;

import butterknife.Bind;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ExchangeRateListActivity extends SearchActivity {

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.empty_view_no_internet)
    View emptyView;

    String filter;

    RealmResults<ExchangeRate> exchangeRates;
    private Realm realm;
    private ExchangeRatesListAdapter adapter;


    @Override
    protected int getLayout() {
        return R.layout.activity_exchange_rate_list;
    }

    @Override
    protected boolean hasParent() {
        return true;
    }

    @Override
    protected void doCreate(@Nullable Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        realm = ExchangeRate.Realm(this);
        exchangeRates = realm.where(ExchangeRate.class).findAllSortedAsync(ExchangeRate.FIELD_CURRENCY_NAME);
        exchangeRates.addChangeListener(changeListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExchangeRatesListAdapter(delegate);
        recyclerView.setAdapter(adapter);
    }

    private ExchangeRatesListAdapter.Delegate delegate = new ExchangeRatesListAdapter.Delegate() {
        @Override
        public Context context() {
            return ExchangeRateListActivity.this;
        }

        @Override
        public void onItemClick
                (BaseAdapter<ExchangeRatesListAdapter.Holder, ExchangeRate> adapter, View view,
                 int position, long id) {

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
                        .findAllSorted(ExchangeRate.FIELD_CURRENCY_NAME);
            }
            if (ret.isEmpty()) {
                com.zealous.utils.ViewUtils.showViews(emptyView);
                ViewUtils.hideViews(recyclerView);
            } else {
                ViewUtils.hideViews(emptyView);
                ViewUtils.showViews(recyclerView);
            }
            return ret;
        }
    };

    final RealmChangeListener<RealmResults<ExchangeRate>> changeListener = new RealmChangeListener<RealmResults<ExchangeRate>>() {
        @Override
        public void onChange(RealmResults<ExchangeRate> element) {
            adapter.notifyDataChanged(filter);
        }
    };

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @Override
    protected void doSearch(String constraint) {
        this.filter = constraint;
        adapter.notifyDataChanged(constraint);
    }
}
