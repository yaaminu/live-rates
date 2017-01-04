package com.zealous.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import java.math.BigDecimal;
import java.math.MathContext;
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

    private Realm realm;
    private ExchangeRate rateTo, rateFrom;
    private String to;
    private String from;

    @Override
    protected boolean hasParent() {
        return true;
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
        GenericUtils.ensureNotEmpty(to, from);
        realm = ExchangeRate.Realm(this);
        TextView title = ButterKnife.findById(this, R.id.title_today);
        title.setText(getString(R.string.today_title, DateUtils.formatDateTime(this, System.currentTimeMillis(),
                FORMAT_SHOW_DATE)));
        lastMonthRate.setText("0.00");
        yesterdayRate.setText("0.00");
        $7daysAgoRate.setText("0.00");

        selfChanged = true;
        currencyFromRate.setText("1.00");
        selfChanged = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDisplay(false);
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    boolean selfChanged = false;

    @OnTextChanged({R.id.tv_currency_from_rate})
    void handleTextChanged(Editable text) {
        if (selfChanged) {
            return;
        }
        refreshDisplay(false);
    }

    @OnTextChanged({R.id.tv_currency_to_rate})
    void handleTextChanged1(Editable text) {
        if (selfChanged) {
            return;
        }
        refreshDisplay(true);
    }

    void refreshDisplay(boolean toChanged) {
        if (!to.equals(currencyToRate.getTag())) { //has changed
            currencyToRate.setTag(to);
            rateTo = realm.where(ExchangeRate.class).equalTo(ExchangeRate.FIELD_CURRENCY_ISO, to).findFirst();
            currencyIconTo.setImageResource(getResources().getIdentifier("drawable/" + rateTo
                    .getCurrencyIso().toLowerCase(Locale.US), null, getPackageName()));
            currencyTo.setText(rateTo.getCurrencyName());
        }
        if (!from.equals(currencyFromRate.getTag())) {
            currencyFromRate.setTag(from);
            rateFrom = realm.where(ExchangeRate.class).equalTo(ExchangeRate.FIELD_CURRENCY_ISO, from).findFirst();
            currencyFrom.setText(rateFrom.getCurrencyName());

            currencyIconFrom.setImageResource(getResources().getIdentifier("drawable/" + rateFrom
                    .getCurrencyIso().toLowerCase(Locale.US), null, getPackageName()));
        }


        selfChanged = true;
        try {
            double inputTo = getDouble(currencyToRate.getText().toString().trim()),
                    inputFrom = getDouble(currencyFromRate.getText().toString().trim());

            if (toChanged) {
                double tmp = BigDecimal.valueOf(inputTo).divide(BigDecimal.valueOf(rateTo.getRate()), MathContext.DECIMAL128)
                        .multiply(BigDecimal.valueOf(rateFrom.getRate())).doubleValue();
                currencyFromRate.setText(FORMAT.format(tmp));

            } else {
                double tmp = BigDecimal.valueOf(inputFrom).divide(BigDecimal.valueOf(rateFrom.getRate()), MathContext.DECIMAL128)
                        .multiply(BigDecimal.valueOf(rateTo.getRate())).doubleValue();
                currencyToRate.setText(FORMAT.format(tmp));
            }
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

    @OnClick({R.id.back, R.id.iv_currency_icon_from, R.id.iv_currency_icon_to})
    void onclick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            default:
                throw new AssertionError();
        }
    }
}
