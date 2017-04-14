package com.zealous.exchangeRates;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.ui.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Map;

import butterknife.Bind;
import io.realm.Realm;

import static com.zealous.exchangeRates.ExchangeRateListActivity.SEARCH;

/**
 * Created by yaaminu on 4/14/17.
 */
public class ExchangeRateFragment extends BaseFragment {

    ExchangeRatesListAdapter adapter;
    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.empty_view_no_internet)
    View emptyView;
    @Bind(R.id.tv_last_updated)
    TextView tvLastUpdated;
    String filter;
    EventBus bus;
    private Realm realm;

    public static ExchangeRateFragment create(EventBus bus) {
        ExchangeRateFragment fragment = new ExchangeRateFragment();
        fragment.bus = bus;
        return fragment;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_exchange_rate_list;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = ExchangeRate.Realm(getContext());
        adapter = new ExchangeRatesListAdapter(new ExchangeRateAdapterDelegateImpl(this, realm));
        bus.register(this);
    }

    @Subscribe
    public void onEvent(Object event) {
        if (event instanceof Map && ((Map) event).containsKey(SEARCH)) {
            adapter.notifyDataChanged((String) ((Map) event).get(SEARCH));
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        realm.close();
        bus.unregister(this);
        super.onDestroy();
    }
}
