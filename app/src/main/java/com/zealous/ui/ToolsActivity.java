package com.zealous.ui;

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
import com.zealous.utils.UiHelpers;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class ToolsActivity extends BaseZealousActivity {

    private List<Tuple> items;

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void doCreate(@Nullable Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        items = new ArrayList<>(3);
        items.add(new Tuple(getString(R.string.exchange_rate_calculator), getString(R.string.exchange_rate_calc_description)));
        items.add(new Tuple(getString(R.string.interest_calc), getString(R.string.interest_calc_des)));
        items.add(new Tuple(getString(R.string.tax_calc), getString(R.string.tax_calc_des)));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

    @Override
    protected int getLayout() {
        return R.layout.activity_tools;
    }

    @Override
    protected boolean hasParent() {
        return true;
    }

    private final SimpleRecyclerViewAdapter.Delegate<Tuple> delegate = new SimpleRecyclerViewAdapter.Delegate<Tuple>() {
        @Override
        public int getLayout() {
            return R.layout.tools_list_item;
        }

        @Override
        public Context context() {
            return ToolsActivity.this;
        }

        @Override
        public void onItemClick(BaseAdapter<SimpleListItemHolder, Tuple> adapter, View view, int position, long id) {
            UiHelpers.showToast("selected" + adapter.getItem(position).getFirst());
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
}
