package com.zealous.expense;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zealous.R;
import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.exchangeRates.ExchangeRateDetailActivity;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import java.math.BigDecimal;
import java.math.MathContext;

import butterknife.ButterKnife;
import io.realm.Realm;

import static android.content.ContentValues.TAG;
import static com.zealous.exchangeRates.ExchangeRate.FORMAT;

/**
 * Created by yaaminu on 5/27/17.
 */

public class InsetRateCalculator extends BottomSheetDialogFragment {
    //    @Bind(R.id.tv_currency_to_rate)
    EditText etCurrencyToRate;
    //    @Bind(R.id.tv_currency_from_rate)
    EditText etCurrencyFromRate;
    //    @Bind(R.id.iv_currency_icon_from)
    ImageView currencyFromIcon;
    //    @Bind(R.id.iv_currency_icon_to)
    ImageView currencyToIcon;


    Realm realm;
    private ExchangeRate toRate;
    private ExchangeRate fromRate;
    private TextView currencyTo;
    private TextView currencyFrom;
    private View viewDetails;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = ExchangeRate.Realm(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.inset_rate_calculator, container, false);
        etCurrencyFromRate = ButterKnife.findById(view, R.id.tv_currency_from_rate);
        etCurrencyToRate = ButterKnife.findById(view, R.id.tv_currency_to_rate);
        currencyFromIcon = ButterKnife.findById(view, R.id.iv_currency_icon_from);
        currencyToIcon = ButterKnife.findById(view, R.id.iv_currency_icon_to);
        currencyTo = ButterKnife.findById(view, R.id.tv_currency_to);
        currencyFrom = ButterKnife.findById(view, R.id.tv_currency_from);
        viewDetails = ButterKnife.findById(view, R.id.view_full_screen);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Bundle arguments = getArguments();
        GenericUtils.ensureNotNull(arguments);
        String to = arguments.getString(ExchangeRateDetailActivity.EXTRA_CURRENCY_TARGET),
                from = arguments.getString(ExchangeRateDetailActivity.EXTRA_CURRENCY_SOURCE),
                startWith = arguments.getString(ExchangeRateDetailActivity.EXTRA_START_WITH);
        toRate = realm.where(ExchangeRate.class).equalTo(ExchangeRate.FIELD_CURRENCY_ISO, to).findFirst();
        fromRate = realm.where(ExchangeRate.class).equalTo(ExchangeRate.FIELD_CURRENCY_ISO, from).findFirst();
        currencyToIcon.setImageResource(toRate.getCurrencyIcon(getContext()));
        currencyFromIcon.setImageResource(fromRate.getCurrencyIcon(getContext()));
        currencyTo.setText(toRate.getCurrencyName());
        currencyFrom.setText(fromRate.getCurrencyName());

        etCurrencyToRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                onCurrencyTo(s.toString());
            }
        });
        etCurrencyFromRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                onCurrencyFrom(s.toString());
            }
        });
        assert to != null;
        if (to.equals(startWith)) {
            etCurrencyToRate.setText("1");
        } else {
            etCurrencyFromRate.setText("1");
        }
        viewDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                Intent intent = new Intent(getContext(), ExchangeRateDetailActivity.class);
                intent.putExtras(arguments);
                String text = etCurrencyFromRate.getText().toString().trim();
                try {
                    double val = Double.parseDouble(GenericUtils.cleanNumberText(text));
                    if (val > 0) {
                        intent.putExtra(ExchangeRateDetailActivity.EXTRA_START_WITH_VALUE, String.valueOf(val));
                    }
                } catch (NumberFormatException e) {
                    PLog.e(TAG, e.getMessage(), e);
                }
                getActivity().startActivity(intent);
            }
        });
    }

    boolean selfChanged = false;

    void onCurrencyTo(String text) {
        if (selfChanged) {
            selfChanged = false;
            return;
        }
        selfChanged = true;
        text = GenericUtils.cleanNumberText(text);
        try {
            if (toRate.getRate() == 0) {
                throw new NumberFormatException();
            }
            etCurrencyFromRate.setText(FORMAT.format(BigDecimal.valueOf(Double.parseDouble(text))
                    .divide(BigDecimal.valueOf(toRate.getRate()), MathContext.DECIMAL128).doubleValue()));
        } catch (NumberFormatException e) {
            etCurrencyFromRate.setText("0");
        }
        etCurrencyFromRate.setSelection(etCurrencyFromRate.getText().length());
    }

    void onCurrencyFrom(String text) {
        if (selfChanged) {
            selfChanged = false;
            return;
        }
        selfChanged = true;
        text = GenericUtils.cleanNumberText(text);
        try {
            double val = Double.parseDouble(text);
            if (toRate.getRate() == 0) {
                throw new NumberFormatException();
            }
            etCurrencyToRate.setText(FORMAT.format(BigDecimal.valueOf(val)
                    .multiply(BigDecimal.valueOf(toRate.getRate()), MathContext.DECIMAL128).doubleValue()));
        } catch (NumberFormatException e) {
            etCurrencyToRate.setText("0");
        }
        etCurrencyFromRate.setSelection(etCurrencyFromRate.getText().length());
    }

    @Override
    public void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
