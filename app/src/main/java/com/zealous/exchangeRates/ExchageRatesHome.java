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
import com.zealous.adapter.SimpleListItemHolder;
import com.zealous.adapter.SimpleRecyclerViewAdapter;
import com.zealous.ui.BaseFragment;

import java.util.List;

import butterknife.Bind;
import io.realm.Realm;

/**
 * Created by yaaminu on 12/20/16.
 */
public class ExchageRatesHome extends BaseFragment {
    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    List<ExchangeRate> items;
    Realm realm;
    SimpleRecyclerViewAdapter.Delegate<ExchangeRate> delegate = new SimpleRecyclerViewAdapter.Delegate<ExchangeRate>() {
        @Override
        public Context context() {
            return getContext();
        }

        @Override
        public void onItemClick(BaseAdapter<SimpleListItemHolder, ExchangeRate> adapter, View view, int position, long id) {

        }

        @Override
        public boolean onItemLongClick(BaseAdapter<SimpleListItemHolder, ExchangeRate> adapter, View view, int position, long id) {
            return false;
        }

        @Override
        public int getLayout() {
            return 0;
        }

        @NonNull
        @Override
        public List<ExchangeRate> dataSet(String constraint) {
            return items;
        }
    };

    @Override
    protected int getLayout() {
        return R.layout.home_exchange_rates;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = ExchangeRate.Realm(getContext());
        items = realm.where(ExchangeRate.class)
                .equalTo(ExchangeRate.FIELD_WATCHING, 1)
                .findAll();
    }

    @Override
    public void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new SimpleExchangeRate(delegate));
    }
}
