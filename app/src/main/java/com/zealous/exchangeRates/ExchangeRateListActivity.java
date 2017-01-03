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
import com.zealous.ui.BaseZealousActivity;
import com.zealous.utils.ViewUtils;

import java.util.List;

import butterknife.Bind;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ExchangeRateListActivity extends BaseZealousActivity {

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.empty_view_no_internet)
    View emptyView;

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
        public List<ExchangeRate> dataSet() {
            if (exchangeRates.isEmpty()) {
                com.zealous.utils.ViewUtils.showViews(emptyView);
                ViewUtils.hideViews(recyclerView);
            } else {
                ViewUtils.hideViews(emptyView);
                ViewUtils.showViews(recyclerView);
            }
            return exchangeRates;
        }
    };

    final RealmChangeListener<RealmResults<ExchangeRate>> changeListener = new RealmChangeListener<RealmResults<ExchangeRate>>() {
        @Override
        public void onChange(RealmResults<ExchangeRate> element) {
            adapter.notifyDataChanged();
        }
    };

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
