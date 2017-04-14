package com.zealous.ui;

import android.content.Context;
import android.content.Intent;
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
import com.zealous.bankRates.InterestRateCalculatorActivity;
import com.zealous.exchangeRates.ExchangeRateDetailActivity;
import com.zealous.utils.UiHelpers;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * Created by yaaminu on 4/14/17.
 */
public class ToolsFragment extends BaseFragment {
    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    private List<Tuple> items;
    private final SimpleRecyclerViewAdapter.Delegate<Tuple> delegate = new SimpleRecyclerViewAdapter.Delegate<Tuple>() {
        @Override
        public int getLayout() {
            return R.layout.tools_list_item;
        }

        @Override
        public Context context() {
            return getContext();
        }

        @Override
        public void onItemClick(BaseAdapter<SimpleListItemHolder, Tuple> adapter, View view, int position, long id) {
            Intent intent = null;
            switch (position) {
                case 0:
                    intent = new Intent(context(), ExchangeRateDetailActivity.class);
                    intent.putExtra(ExchangeRateDetailActivity.EXTRA_CURRENCY_SOURCE, "GHS");
                    break;
                case 1:
                    intent = new Intent(context(), InterestRateCalculatorActivity.class);
                    break;
                case 3:
                    UiHelpers.showToast("selected" + adapter.getItem(position).getFirst());
                    break;
                default:
                    throw new AssertionError();
            }
            startActivity(intent);
        }

        @Override
        public boolean onItemLongClick(BaseAdapter<SimpleListItemHolder, Tuple> adapter, View view, int position, long id) {
            return false;
        }

        @NonNull
        @Override
        public List<Tuple> dataSet(String constraint) {
            return items;
        }
    };

    @Override
    protected int getLayout() {
        return R.layout.fragment_tools;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        items = new ArrayList<>(3);
        items.add(new Tuple(getString(R.string.exchange_rate_calculator), getString(R.string.exchange_rate_calc_description)));
        items.add(new Tuple(getString(R.string.interest_calc), getString(R.string.interest_calc_des)));
        items.add(new Tuple(getString(R.string.tax_calc), getString(R.string.tax_calc_des)));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new ToolsRecyclerViewAdapter(delegate));
    }

    static class ToolsRecyclerViewAdapter extends SimpleRecyclerViewAdapter<Tuple> {
        public ToolsRecyclerViewAdapter(SimpleRecyclerViewAdapter.Delegate<Tuple> delegate) {
            super(delegate);
        }

        @Override
        protected void doBindHolder(SimpleListItemHolder holder, int position) {
            Tuple item = getItem(position);
            holder.first.setText(item.getFirst());
            holder.second.setText(item.getSecond());
        }
    }
}
