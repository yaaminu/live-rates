package com.zealous.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;
import com.zealous.adapter.HomeRecyclerViewAdapter;
import com.zealous.exchangeRates.ExchangeRateListActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * Created by yaaminu on 12/20/16.
 */

public class
HomeMenuItemsFragment extends BaseFragment {


    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @DrawableRes
    private final int[] drawables = {
            R.drawable.ic_expense_tracker_24dp,
            R.drawable.ic_live_rates_24dp,
            R.drawable.ic_bog_rates_24dp,
            R.drawable.ic_other_rates_24dp,
            R.drawable.ic_calculators_24dp
    };

    private List<HomeItem> homeItems;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] titles = getResources().getStringArray(R.array.home_menu_titles);
        homeItems = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            homeItems.add(new HomeItem(titles[i], drawables[i], MainActivity.colors[i]));
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.home_menu;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HomeRecyclerViewAdapter adapter = new HomeRecyclerViewAdapter(delegate);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(adapter);
    }

    private final HomeRecyclerViewAdapter.Delegate delegate = new HomeRecyclerViewAdapter.Delegate() {
        @Override
        public Context context() {
            return getActivity();
        }

        @Override
        public void onItemClick(BaseAdapter<HomeRecyclerViewAdapter.VHolder, HomeItem> adapter, View view, int position, long id) {
            if (position == 1) {
                Intent intent = new Intent(getContext(), ExchangeRateListActivity.class);
                startActivity(intent);
            } else if (position == 4) {
                Intent intent = new Intent(getContext(), ToolsActivity.class);
                startActivity(intent);
            }
        }

        @Override
        public boolean onItemLongClick(BaseAdapter<HomeRecyclerViewAdapter.VHolder, HomeItem> adapter, View view, int position, long id) {
            return false;
        }

        @NonNull
        @Override
        public List<HomeItem> dataSet() {
            return homeItems;
        }
    };
}
