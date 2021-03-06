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

import javax.inject.Inject;

import butterknife.BindView;
import io.realm.Realm;
import io.realm.RealmChangeListener;

import static com.zealous.exchangeRates.ExchangeRateListActivity.SEARCH;

/**
 * Created by yaaminu on 4/14/17.
 */
public class ExchangeRateFragment extends BaseFragment {

    @Inject
    EventBus bus;
    @Inject
    Realm realm;
    @Inject
    ExchangeRatesListAdapter adapter;
    @Inject
    ExchangeRateManager ratesManager;

    private final RealmChangeListener<Realm> changeListener = new RealmChangeListener<Realm>() {
        @Override
        public void onChange(Realm element) {
            adapter.notifyDataChanged("");
        }
    };
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view_no_internet)
    View emptyView;
    @BindView(R.id.tv_last_updated)
    TextView tvLastUpdated;

    @Override
    protected int getLayout() {
        return R.layout.activity_exchange_rate_list;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerExchangeRateFragmentComponent.builder()
                .exchangeRateFragmentModule(new ExchangeRateFragmentModule(this))
                .build()
                .inject(this);
        bus.register(this);
        ratesManager.loadRates();
    }

    @Override
    public void onResume() {
        super.onResume();
        realm.addChangeListener(changeListener);
    }

    @Override
    public void onPause() {
        realm.removeChangeListener(changeListener);
        super.onPause();
    }

    @Subscribe
    public void onEvent(Object event) {
        if (event instanceof Map && ((Map) event).containsKey(SEARCH)) {
            refreshDisplay(((String) ((Map) event).get(SEARCH)));
        }
    }

    private void refreshDisplay(String filter) {
        adapter.notifyDataChanged(filter);
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
