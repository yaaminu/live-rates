package com.zealous.exchangeRates;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zealous.R;
import com.zealous.adapter.BaseAdapter;
import com.zealous.adapter.SimpleListItemHolder;
import com.zealous.adapter.SimpleRecyclerViewAdapter;
import com.zealous.ui.BaseFragment;
import com.zealous.utils.GenericUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.Bind;

/**
 * @author by yaaminu on 1/4/17.
 */
public class HistoricalRateMontFragment extends BaseFragment {

    public static final String ARG_MONTH = "month";
    public static final String ARG_CURRENT_YEAR = "currentYear";
    public static final String ARG_TO = "to";
    public static final String ARG_FROM = "from";
    public static final String RATE_TO = "rateTo";
    private final EventBus eventBus = EventBus.builder().build();
    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    List<HistoricalRateTuple> historicalRates;
    private ExchangeRateManager exchangeRateManager = new ExchangeRateManager(eventBus);
    private int month, year;
    private int days;
    private HistoricalRatesAdapter adapter;
    private String to;
    private String from;
    private double rateTo;
    private final HistoricalRatesAdapter.Delegate delegate
            = new HistoricalRatesAdapter.Delegate() {
        @Override
        public double rateTo() {
            return rateTo;
        }

        @Override
        public int getLayout() {
            return R.layout.historical__rate_list_item;
        }

        @Override
        public Context context() {
            return getContext();
        }

        @Override
        public void onItemClick(BaseAdapter<SimpleListItemHolder, HistoricalRateTuple> adapter, View view, int position, long id) {

        }

        @Override
        public boolean onItemLongClick(BaseAdapter<SimpleListItemHolder, HistoricalRateTuple> adapter, View view, int position, long id) {
            return false;
        }

        @NonNull
        @Override
        public List<HistoricalRateTuple> dataSet(String constrain) {
            return historicalRates;
        }

    };

    @NonNull
    public static Fragment create(int currentYear, int currentMonth, double rateTo, String to, String from) {
        Fragment fragment = new HistoricalRateMontFragment();
        Bundle args = new Bundle(2);
        args.putInt(ARG_CURRENT_YEAR, currentYear);
        args.putInt(ARG_MONTH, currentMonth);
        args.putString(ARG_TO, to);
        args.putString(ARG_FROM, from);
        args.putDouble(RATE_TO, rateTo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayout() {
        return R.layout.historical_rates_fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        month = getArguments().getInt(ARG_MONTH);
        year = getArguments().getInt(ARG_CURRENT_YEAR);
        GenericUtils.ensureConditionTrue(month >= 0 && year > 0, "invalid year or month");

        to = getArguments().getString("to");
        from = getArguments().getString("from");
        GenericUtils.ensureNotEmpty(to, from);

        rateTo = getArguments().getDouble(RATE_TO);
        GenericUtils.ensureConditionTrue(rateTo > 0, "invalid input");

        final GregorianCalendar cal = new GregorianCalendar();
        final int currentMonth = cal.get(Calendar.MONTH);


        if (month == currentMonth) {
            days = cal.get(Calendar.DAY_OF_MONTH);
        } else {
            days = getNumOfDaysInMonth(month, cal.isLeapYear(cal.get(Calendar.YEAR)));
        }
        historicalRates = new ArrayList<>(days);
        eventBus.register(this);
        exchangeRateManager.loadHistoricalRates(from, to, year, month, days);
        fillHistoricalRates();
        adapter = new HistoricalRatesAdapter(delegate);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 5
                , GridLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
    }

    private int getNumOfDaysInMonth(int month, boolean isLeap) {
        switch (month) {
            case 0:
            case 2:
            case 4:
            case 6:
            case 7:
            case 9:
            case 11:
                return 31;
            case 1:
                return isLeap ? 29 : 28;
            case 3:
            case 5:
            case 8:
            case 10:
                return 30;
            default:
                throw new AssertionError();
        }
    }


    @Override
    public void onDestroyView() {
        eventBus.unregister(this);
        super.onDestroyView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        if (event instanceof HistoricalRateTuple) {
            //noinspection unchecked
            if (((HistoricalRateTuple) event).to.equals(to) && ((HistoricalRateTuple) event).from.equals(from)) {
                historicalRates.set(((HistoricalRateTuple) event).index - 1, (HistoricalRateTuple) event);
                adapter.notifyDataChanged("");
            }
        }
    }

    private void fillHistoricalRates() {
        for (int i = days; i > 0; i--) {
            historicalRates.add(new HistoricalRateTuple(to, from, 0, i));
        }
    }

    static class HistoricalRatesAdapter extends SimpleRecyclerViewAdapter<HistoricalRateTuple> {

        private final Delegate delegate;

        public HistoricalRatesAdapter(Delegate delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        @Override
        protected void doBindHolder(SimpleListItemHolder holder, int position) {
            final HistoricalRateTuple item = getItem(position);
            holder.first.setText(item.rate == 0 ? "??" : ExchangeRate.FORMAT.format(BigDecimal.valueOf(delegate.rateTo())
                    .divide(BigDecimal.valueOf(item.rate), MathContext.DECIMAL128)));
            holder.second.setText(String.valueOf(item.index));
        }

        public interface Delegate extends SimpleRecyclerViewAdapter.Delegate<HistoricalRateTuple> {
            double rateTo();
        }
    }
}

