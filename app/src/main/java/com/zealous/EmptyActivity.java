package com.zealous;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.zealous.exchangeRates.ExchangeRate;

import io.realm.Realm;

public class EmptyActivity extends AppCompatActivity {

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);
        realm = ExchangeRate.Realm(this);
    }

    @Override
    protected void onDestroy() {
        realm.close();
    }
}
