package com.zealous.bankRates;

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
import com.zealous.adapter.Tuple;
import com.zealous.ui.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * Created by yaaminu on 12/20/16.
 */
public class BankRatesFragment extends BaseFragment {
    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    List<Tuple> items;

    @Override
    protected int getLayout() {
        return R.layout.bank_rates_fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        items = new ArrayList<>(3);
        items.clear();
        items.add(new Tuple("Interest Rate", "44.4%"));
        items.add(new Tuple("Treasury Bill Rate(91 days)", "25.9%"));
        items.add(new Tuple("Treasury Bill Rate(181 days)", "24.5%"));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new SimpleRecyclerViewAdapter<>(delegate));
    }

    private final SimpleRecyclerViewAdapter.Delegate<Tuple> delegate = new SimpleRecyclerViewAdapter.Delegate<Tuple>() {
        @Override
        public Context context() {
            return getContext();
        }

        @Override
        public void onItemClick(BaseAdapter<SimpleListItemHolder, Tuple> adapter, View view, int position, long id) {

        }

        @Override
        public boolean onItemLongClick(BaseAdapter<SimpleListItemHolder, Tuple> adapter, View view, int position, long id) {
            return false;
        }

        @Override
        public int getLayout() {
            return 0;
        }

        @NonNull
        @Override
        public List<Tuple> dataSet() {
            return items;
        }
    };
}
