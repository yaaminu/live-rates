package com.zealous.exchangeRates;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.zealous.R;
import com.zealous.ui.BaseZealousActivity;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.realm.Realm;

import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static com.zealous.exchangeRates.ExchangeRate.FORMAT;

public class ExchangeRateDetailActivity extends BaseZealousActivity {

    public static final String EXTRA_CURRENCY_TARGET = "currency_target";
    public static final String EXTRA_CURRENCY_SOURCE = "currency_source";
    public static final String TAG = ExchangeRateDetailActivity.class.getSimpleName();
    public static final int PICK_EXCHANGE_RATE_REQUEST_FROM = 1001;
    private static final int PICK_EXCHANGE_RATE_REQUEST_TO = 1002;
    @Bind(R.id.tv_currency_from_rate)
    EditText currencyFromRate;
    @Bind(R.id.tv_currency_to_rate)
    EditText currencyToRate;
    @Bind(R.id.tv_currency_from)
    TextView currencyFrom;
    @Bind(R.id.tv_currency_to)
    TextView currencyTo;

    @Bind(R.id.iv_currency_icon_to)
    ImageView currencyIconTo;
    @Bind(R.id.iv_currency_icon_from)
    ImageView currencyIconFrom;

    @Bind(R.id.tv_yesterday_rate)
    TextView yesterdayRate;
    @Bind(R.id.tv_7_days_ago_rate)
    TextView $7daysAgoRate;
    @Bind(R.id.tv_last_month_rate)
    TextView lastMonthRate;

    @Bind(R.id.graph_view)
    com.github.mikephil.charting.charts.LineChart lineChartView;

    @Bind(R.id.drawer)
    DrawerLayout drawer;
    List<ExchangeRate> historicalRates;
    boolean selfChanged = false;
    ViewPager pager;
    @Nullable
    ExchangeRate rateFrom;
    private Realm realm;
    @Nullable
    private ExchangeRate rateTo;
    private String to;
    private String from;

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
        to = to == null ? "" : to;
        from = from == null ? "" : from;
        realm = ExchangeRate.Realm(this);
        TextView title = ButterKnife.findById(this, R.id.title_today);
        title.setText(getString(R.string.today_title, DateUtils.formatDateTime(this, System.currentTimeMillis(),
                FORMAT_SHOW_DATE)));
        historicalRates = new ArrayList<>(30);
        selfChanged = true;
        currencyFromRate.setText(R.string.base);
        selfChanged = false;
        setUpStatusBarColor(R.color.exchangeRatePrimaryDark);
        //noinspection ConstantConditions
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.exchangeRatePrimary));
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.historical_rates_title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDisplay(false);
        EventBus.getDefault().register(this);
        if (!TextUtils.isEmpty(to) && !TextUtils.isEmpty(from)) {
            ExchangeRateManager.loadHistoricalRates(from, to);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        //noinspection unchecked,unchecked
        historicalRates = ((List<ExchangeRate>) event);
        refreshDisplay(false);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
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
                currencyIconTo.setImageResource(getResources().getIdentifier("drawable/" + rateTo
                        .getCurrencyIso().toLowerCase(Locale.US), null, getPackageName()));
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

                currencyIconFrom.setImageResource(getResources().getIdentifier("drawable/" + rateFrom
                        .getCurrencyIso().toLowerCase(Locale.US), null, getPackageName()));
            } else {
                currencyFrom.setText(R.string.choose_currency);
                currencyIconFrom.setImageResource(R.drawable.ghs);
            }
        }


        selfChanged = true;
        try {
            double inputTo = getDouble(currencyToRate.getText().toString().trim()),
                    inputFrom = getDouble(currencyFromRate.getText().toString().trim());

            double tmp;
            if (toChanged) {
                tmp = BigDecimal.valueOf(inputTo).divide(BigDecimal.valueOf(rateTo.getRate()), MathContext.DECIMAL128)
                        .multiply(BigDecimal.valueOf(rateFrom.getRate())).doubleValue();
                currencyFromRate.setText(FORMAT.format(tmp));

            } else {
                tmp = BigDecimal.valueOf(inputFrom).divide(BigDecimal.valueOf(rateFrom.getRate()), MathContext.DECIMAL128)
                        .multiply(BigDecimal.valueOf(rateTo.getRate())).doubleValue();
                currencyToRate.setText(FORMAT.format(tmp));
            }
            if (!historicalRates.isEmpty()) {
                yesterdayRate.setText(FORMAT.format(historicalRates.get(0).getRate()));
                $7daysAgoRate.setText(FORMAT.format(historicalRates.get(5).getRate()));
                lastMonthRate.setText(FORMAT.format(historicalRates.get(26).getRate()));
                plotGraph();
            } else {
                yesterdayRate.setText(FORMAT.format(0));
                $7daysAgoRate.setText(FORMAT.format(0));
                lastMonthRate.setText(FORMAT.format(0));
                clearGraph();
            }
        } catch (Exception e) {
            PLog.e(TAG, e.getMessage(), e);
        } finally {
            selfChanged = false;
        }
    }

    double getDouble(String text) {
        try {
            return Double.parseDouble(text);
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
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupFragmentPagerAdapter() {
        if (pager == null) {
            pager = ButterKnife.findById(this, R.id.pager);
            TabLayout tablayout = ButterKnife.findById(this, R.id.tab_strip);
            pager.setAdapter(new FragmentPagerAdapterCustom(getSupportFragmentManager(), getResources().getStringArray(R.array.months)));
            tablayout.setupWithViewPager(pager);
        }
    }

    void plotGraph() {
        List<Entry> pointValues = new ArrayList<>(historicalRates.size());
        for (int i = 0; i < historicalRates.size(); i++) {
            pointValues.add(new Entry(i, ((float) historicalRates.get(i).getRate())));
        }
        LineDataSet line = new LineDataSet(pointValues, "rates");
        LineData data = new LineData(line);
        lineChartView.setData(data);
        lineChartView.invalidate();
    }

    void clearGraph() {
        LineData lineCharData = new LineData();
        lineChartView.setData(lineCharData);
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

        @SuppressWarnings("WeakerAccess")
        public FragmentPagerAdapterCustom(FragmentManager fragmentManager, String[] titles) {
            super(fragmentManager);
            Calendar calendar = Calendar.getInstance(Locale.US);
            currentMonth = calendar.get(Calendar.MONTH);
            currentYear = calendar.get(Calendar.YEAR);
            this.titles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return HistoricalRateMontFragment.create(currentYear, position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return currentMonth + 1;
        }
    }
}
