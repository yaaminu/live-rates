package com.zealous.bankRates;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.format.DateUtils;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.ui.BaseZealousActivity;

import java.math.BigDecimal;
import java.math.MathContext;

import butterknife.Bind;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;

public class InterestRateCalculatorActivity extends BaseZealousActivity {

    @Bind(R.id.tv_interest)
    TextView interest;
    @Bind(R.id.tv_amount)
    TextView amount;
    @NonNull
    InterestCalculator calculator = new SimpleInterestRateCalculator();

    @Bind(R.id.sp_days_month_year)
    Spinner durationSpinner;

    @Bind(R.id.et_duration)
    EditText duration;
    @Bind(R.id.et_rate)
    EditText etRate;

    @Bind(R.id.title_today)
    TextView title;

    @Override
    protected int getLayout() {
        return R.layout.activity_interest_rate_calc;
    }

    @OnTextChanged(R.id.et_rate)
    void rateChanged(Editable text) {
        double value = getDoubleSafe(text);
        if (value > 100) {
            calculator.setRate(0);
            etRate.setError(getString(R.string.error_rate_exceeds_100));
        } else {
            calculator.setRate(BigDecimal.valueOf(value).divide(BigDecimal.valueOf(100), MathContext.DECIMAL128).doubleValue());
        }
        refreshDisplay();
    }

    @OnTextChanged(R.id.et_input)
    void onPrincipalChanged(Editable text) {
        calculator.setPrincipal(getDoubleSafe(text));
        refreshDisplay();
    }

    @OnTextChanged(R.id.et_duration)
    void durationChanged(CharSequence text) {
        double value = getDoubleSafe(text);
        if (value != 0) {
            switch (durationSpinner.getSelectedItemPosition()) {
                case 0:
                    value /= 360;
                    break;
                case 1:
                    value /= 12;
                    break;
                case 2:
                    value /= 1;
                    break;
                default:
                    throw new AssertionError();
            }
        }
        calculator.setDuration(value);
        refreshDisplay();
    }

    @OnItemSelected(R.id.sp_days_month_year)
    void onDurationTypeSelected() {
        durationChanged(duration.getText());
    }

    @OnItemSelected(R.id.sp_rate_type)
    void onItemSelected(int position) {
        InterestCalculator tmp = calculator;
        switch (position) {
            case 0:
                calculator = new SimpleInterestRateCalculator();
                break;
            case 1:
                calculator = new CompoundInterestRateCalculator();
                break;
            case 2:
                calculator = new TreasuryBillCalculator();
                break;
            default:
                throw new AssertionError();
        }

        calculator.setDuration(tmp.getDuration());
        calculator.setPrincipal(tmp.getPrincipal());
        calculator.setRate(tmp.getRate());
        refreshDisplay();
    }


    @Override
    protected boolean hasParent() {
        return true;
    }

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.interest_rate_calculator);
        calculator = new SimpleInterestRateCalculator();
        title.setText(getString(R.string.title_today, DateUtils.formatDateTime(this, System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDisplay();
    }

    protected void refreshDisplay() {
        interest.setText(getString(R.string.interest_amount, ExchangeRate.FORMAT.format(calculator.getInterest().doubleValue())));
        amount.setText(getString(R.string.amount_value, ExchangeRate.FORMAT.format(calculator.getAmount().doubleValue())));
    }

    private double getDoubleSafe(CharSequence text) {
        double value;
        try {
            value = Double.parseDouble(text.toString().trim());
        } catch (NumberFormatException e) {
            value = 0;
        }
        return value;
    }

}
