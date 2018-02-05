package com.zealous.exchangeRates;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.zealous.R;
import com.zealous.ui.BaseZealousActivity;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.realm.Realm;

import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;
import static com.zealous.exchangeRates.ExchangeRate.FORMAT;

public class ExchangeRateDetailActivity extends BaseZealousActivity {

    public static final String EXTRA_CURRENCY_TARGET = "currency_target";
    public static final String EXTRA_CURRENCY_SOURCE = "currency_source";
    public static final String TAG = ExchangeRateDetailActivity.class.getSimpleName();
    public static final int PICK_EXCHANGE_RATE_REQUEST_FROM = 1001;
    public static final String EXTRA_START_WITH = "startWith";
    private static final int PICK_EXCHANGE_RATE_REQUEST_TO = 1002;
    public static final String EXTRA_START_WITH_VALUE = "extra_start_with_value";
    @NonNull
    private final EventBus eventBus = EventBus.builder().build();
    @NonNull
    private final ExchangeRateManager exchangeRateManager = new ExchangeRateManager(eventBus);
    @BindView(R.id.tv_currency_from_rate)
    EditText currencyFromRate;
    @BindView(R.id.tv_currency_to_rate)
    EditText currencyToRate;
    @BindView(R.id.tv_currency_from)
    TextView currencyFrom;
    @BindView(R.id.tv_currency_to)
    TextView currencyTo;
    @BindView(R.id.iv_currency_icon_to)
    ImageView currencyIconTo;
    @BindView(R.id.iv_currency_icon_from)
    ImageView currencyIconFrom;
    @BindView(R.id.tv_yesterday_rate)
    TextView yesterdayRate;
    @BindView(R.id.tv_7_days_ago_rate)
    TextView $7daysAgoRate;
    @BindView(R.id.tv_last_month_rate)
    TextView lastMonthRate;
    @BindView(R.id.graph_view)
    com.github.mikephil.charting.charts.LineChart lineChartView;
    @BindView(R.id.drawer)
    DrawerLayout drawer;
    @BindView(R.id.stale_rates)
    TextView staleRates;

    List<HistoricalRateTuple> historicalRates;
    boolean selfChanged = false;
    ViewPager pager;
    @Nullable
    ExchangeRate rateFrom;
    private Realm realm;
    @Nullable
    private ExchangeRate rateTo;
    private String to;
    private String from;
    private String startWith;
    private String startWithValue;

    @Override
    protected boolean hasParent() {
        return false;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_exchange_rate_detail;
    }

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        to = getIntent().getStringExtra(EXTRA_CURRENCY_TARGET);
        from = getIntent().getStringExtra(EXTRA_CURRENCY_SOURCE);
        startWithValue = getIntent().getStringExtra(EXTRA_START_WITH_VALUE);
        to = to == null ? "" : to;
        from = from == null ? "" : from;
        realm = ExchangeRate.Realm(this);
        TextView title = ButterKnife.findById(this, R.id.title_today);
        long lastUpdated = ExchangeRateManager.lastUpdated();
        if (lastUpdated <= 0) {
            lastUpdated = System.currentTimeMillis();
        }

        staleRates.setVisibility((lastUpdated > 0 && lastUpdated <= System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))
                ? View.VISIBLE : View.GONE);

        title.setText(getString(R.string.today_title, DateUtils.formatDateTime(this, lastUpdated,
                FORMAT_SHOW_DATE | FORMAT_SHOW_WEEKDAY)));
        historicalRates = new ArrayList<>(28);
        fillRates();
        startWith = getIntent().getStringExtra(EXTRA_START_WITH);
        setUpStatusBarColor(R.color.exchangeRatePrimaryDark);
        //noinspection ConstantConditions
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.exchangeRatePrimary));
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.historical_rates_title);
        int width = getResources().getDisplayMetrics().widthPixels;
        final View historyContainer = ButterKnife.findById(this, R.id.history_container);
        DrawerLayout.LayoutParams params = ((DrawerLayout.LayoutParams) historyContainer.getLayoutParams());
        params.width = width;
        historyContainer.setLayoutParams(params);
        final Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_clear_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(GravityCompat.END);
            }
        });
    }

    private void fillRates() {
        if (!TextUtils.isEmpty(to) && !TextUtils.isEmpty(from)) {
            historicalRates.clear();
            for (int i = 28; i > 0; i--) {
                historicalRates.add(new HistoricalRateTuple(to, from, 0, i));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean toChanged = false;
        if (!GenericUtils.isEmpty(startWithValue)) {
            selfChanged = true;
            toChanged = false;
            currencyFromRate.setText(startWithValue);
            currencyFromRate.setSelection(currencyFromRate.getText().length());
            startWithValue = null; //next on resume will not use it
            selfChanged = false;
        } else if (!GenericUtils.isEmpty(startWith)) {
            selfChanged = true;
            if (startWith.equals(to)) {
                toChanged = true;
                currencyToRate.setText(R.string.base);
                currencyToRate.setSelection(currencyToRate.getText().length());
            } else if (startWith.equals(from)) {
                toChanged = false;
                currencyFromRate.setText(R.string.base);
                currencyFromRate.setSelection(currencyFromRate.getText().length());
            }
            selfChanged = false;
        }
        refreshDisplay(toChanged);
        eventBus.register(this);
        if (!TextUtils.isEmpty(to) && !TextUtils.isEmpty(from)) {
            exchangeRateManager.loadHistoricalRates(from, to);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        if (event instanceof HistoricalRateTuple) {
            //noinspection unchecked,unchecked
            if (((HistoricalRateTuple) event).to.equals(to) && ((HistoricalRateTuple) event).from.equals(from)) {
                historicalRates.set(((HistoricalRateTuple) event).index - 1, (HistoricalRateTuple) event);
            }
            refreshDisplay(true);
        }
    }

    @Override
    protected void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @OnTextChanged({R.id.tv_currency_from_rate})
    void handleTextChanged(@SuppressWarnings("UnusedParameters") Editable text) {
        if (selfChanged) {
            return;
        }
        refreshDisplay(false);
    }

    @OnTextChanged({R.id.tv_currency_to_rate})
    void handleTextChanged1(@SuppressWarnings("UnusedParameters") Editable text) {
        if (selfChanged) {
            return;
        }
        refreshDisplay(true);
    }

    void refreshDisplay(boolean toChanged) {
        if (!to.equals(currencyToRate.getTag())) { //has changed
            currencyToRate.setTag(to);
            rateTo = realm.where(ExchangeRate.class).equalTo(ExchangeRate.FIELD_CURRENCY_ISO, to).findFirst();
            if (rateTo != null) {
                currencyIconTo.setImageResource(rateTo.getCurrencyIcon(this));
                currencyTo.setText(rateTo.getCurrencyName());
            } else {
                currencyTo.setText(R.string.choose_currency);
                currencyIconTo.setImageResource(R.drawable.ghs);
            }
        }
        if (!from.equals(currencyFromRate.getTag())) {
            currencyFromRate.setTag(from);
            rateFrom = realm.where(ExchangeRate.class).equalTo(ExchangeRate.FIELD_CURRENCY_ISO, from).findFirst();
            if (rateFrom != null) {
                currencyFrom.setText(rateFrom.getCurrencyName());

                currencyIconFrom.setImageResource(rateFrom.getCurrencyIcon(this));
            } else {
                currencyFrom.setText(R.string.choose_currency);
                currencyIconFrom.setImageResource(R.drawable.ghs);
            }
        }


        selfChanged = true;
        try {
            updateValues(toChanged);
        } catch (Exception e) {
            PLog.e(TAG, e.getMessage(), e);
        } finally {
            selfChanged = false;
        }
    }

    private void updateValues(boolean toChanged) {
        double inputTo = getDouble(currencyToRate.getText().toString().trim()),
                inputFrom = getDouble(currencyFromRate.getText().toString().trim());

        assert rateFrom != null;
        assert rateTo != null;
        double tmp;
        if (toChanged) {
            tmp = BigDecimal.valueOf(inputTo).divide(BigDecimal.valueOf(rateTo.getRate()), MathContext.DECIMAL128)
                    .multiply(BigDecimal.valueOf(rateFrom.getRate())).doubleValue();
            currencyFromRate.setText(FORMAT.format(tmp));
            currencyFromRate.setSelection(currencyFromRate.getText().length());
            inputFrom = tmp;
        } else {
            tmp = BigDecimal.valueOf(inputFrom).divide(BigDecimal.valueOf(rateFrom.getRate()), MathContext.DECIMAL128)
                    .multiply(BigDecimal.valueOf(rateTo.getRate())).doubleValue();
            currencyToRate.setText(FORMAT.format(tmp));
            currencyToRate.setSelection(currencyToRate.getText().length());
            inputTo = tmp;
        }
        if (!historicalRates.isEmpty() && inputFrom > 0) {
            yesterdayRate.setText(FORMAT.format(getHistory(inputTo, historicalRates.get(1).rate)));
            $7daysAgoRate.setText(FORMAT.format(getHistory(inputTo, historicalRates.get(7).rate)));
            lastMonthRate.setText(FORMAT.format(getHistory(inputTo, historicalRates.get(27).rate)));
            plotGraph(inputTo);
        } else {
            yesterdayRate.setText(FORMAT.format(0));
            $7daysAgoRate.setText(FORMAT.format(0));
            lastMonthRate.setText(FORMAT.format(0));
            clearGraph();
        }
    }

    private double getHistory(double inputTo, double historicalRate) {
        assert rateFrom != null;
        if (historicalRate == 0) return 0;
        return BigDecimal.valueOf(inputTo).divide(BigDecimal.valueOf(historicalRate), MathContext.DECIMAL128).doubleValue();
    }

    double getDouble(String text) {
        try {
            return Double.parseDouble(GenericUtils.cleanNumberText(text));
        } catch (NumberFormatException e) {
            PLog.e(TAG, e.getMessage(), e);
            return 0.00;
        }
    }

    @OnClick({R.id.back, R.id.tv_currency_to, R.id.tv_currency_from, R.id.open_history, R.id.iv_currency_icon_from, R.id.iv_currency_icon_to})
    void onclick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.open_history:
                if (!drawer.isDrawerOpen(GravityCompat.END)) {
                    setupFragmentPagerAdapter();
                    drawer.openDrawer(GravityCompat.END);
                }
                break;
            case R.id.iv_currency_icon_from: //fall through
            case R.id.tv_currency_from:
                Intent intent = new Intent(this, ExchangeRateListActivity.class);
                intent.setAction(ExchangeRateListActivity.EXTRA_PICK_CURRENCY);
                startActivityForResult(intent, PICK_EXCHANGE_RATE_REQUEST_FROM);
                break;
            case R.id.iv_currency_icon_to: //fall through
            case R.id.tv_currency_to:
                intent = new Intent(this, ExchangeRateListActivity.class);
                intent.setAction(ExchangeRateListActivity.EXTRA_PICK_CURRENCY);
                startActivityForResult(intent, PICK_EXCHANGE_RATE_REQUEST_TO);
                break;
            default:
                throw new AssertionError();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_EXCHANGE_RATE_REQUEST_FROM || requestCode == PICK_EXCHANGE_RATE_REQUEST_TO) {
            if (resultCode == RESULT_OK) {
                String ret = data.getStringExtra(ExchangeRateListActivity.EXTRA_SELECTED);
                if (requestCode == PICK_EXCHANGE_RATE_REQUEST_TO) {
                    to = ret;
                } else {
                    from = ret;
                }
                fillRates();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupFragmentPagerAdapter() {
        if (!GenericUtils.isEmpty(to) && !GenericUtils.isEmpty(from)) {
            pager = ButterKnife.findById(this, R.id.pager);
            TabLayout tablayout = ButterKnife.findById(this, R.id.tab_strip);
            pager.setAdapter(new FragmentPagerAdapterCustom(this, getResources().getStringArray(R.array.months), to, from));
            tablayout.setupWithViewPager(pager);
        } else {
            Toast.makeText(getApplicationContext(), R.string.no_currency_selected
                    , Toast.LENGTH_SHORT).show();
        }
    }

    void plotGraph(double inputTo) {
        List<Entry> pointValues = new ArrayList<>(historicalRates.size());
        for (int i = 0; i < historicalRates.size(); i++) {
            pointValues.add(new Entry(i + 1, (float) getHistory(inputTo, historicalRates.get(i).rate)));
        }
        LineDataSet line = new LineDataSet(pointValues, "rates");
        LineData data = new LineData(line);
        lineChartView.setData(data);
        lineChartView.invalidate();
    }

    void clearGraph() {
        LineData lineCharData = new LineData();
        lineChartView.setData(lineCharData);
        lineChartView.invalidate();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
            return;
        }
        super.onBackPressed();
    }

    static class FragmentPagerAdapterCustom extends FragmentStatePagerAdapter {

        private final int currentMonth;
        private final int currentYear;
        private final String[] titles;
        private final String to;
        private final String from;
        private final ExchangeRateDetailActivity activity;

        @SuppressWarnings("WeakerAccess")
        public FragmentPagerAdapterCustom(ExchangeRateDetailActivity activity, String[] titles, String to, String from) {
            super(activity.getSupportFragmentManager());
            this.activity = activity;
            Calendar calendar = Calendar.getInstance(Locale.US);
            currentMonth = calendar.get(Calendar.MONTH);
            currentYear = calendar.get(Calendar.YEAR);
            this.titles = titles;
            this.to = to;
            this.from = from;
        }

        @Override
        public Fragment getItem(int position) {
            return HistoricalRateMontFragment.create(currentYear, currentMonth - position, activity.getDouble(activity.currencyToRate.getText().toString()), to, from);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[currentMonth - position];
        }

        @Override
        public int getCount() {
            return currentMonth + 1;
        }
    }
}
