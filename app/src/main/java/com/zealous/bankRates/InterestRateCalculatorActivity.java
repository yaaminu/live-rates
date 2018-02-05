package com.zealous.bankRates;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.ui.BaseZealousActivity;

import java.math.BigDecimal;
import java.math.MathContext;

import butterknife.BindView;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;

public class InterestRateCalculatorActivity extends BaseZealousActivity {

    @BindView(R.id.tv_interest)
    TextView interest;
    @BindView(R.id.tv_amount)
    TextView amount;
    @NonNull
    InterestCalculator calculator = new CompoundInterestRateCalculator();

    @BindView(R.id.sp_days_month_year)
    Spinner durationSpinner;

    @BindView(R.id.et_duration)
    EditText duration;
    @BindView(R.id.et_rate)
    EditText etRate;

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


    @Override
    protected boolean hasParent() {
        return true;
    }

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.interest_rate_calculator);
        calculator = new CompoundInterestRateCalculator();
        setUpStatusBarColor(R.color.calculatorsPrimaryDark);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.calculatorsPrimary));
        }
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
