package com.zealous.ui

import com.zealous.exchangeRates.ExchangeRate
import com.zealous.exchangeRates.ExchangeRateManager
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import org.greenrobot.eventbus.EventBus

class WatchedCurrencies(realm: Realm) : BaseRealmLiveData<ExchangeRate>(realm) {
    override fun load(): RealmResults<ExchangeRate> {
        return realm.where(ExchangeRate::class.java)
                .equalTo(ExchangeRate.FIELD_WATCHING, 1)
                .findAllSortedAsync(ExchangeRate.FIELD_CURRENCY_NAME, Sort.ASCENDING)
    }

    override fun onActive() {
        super.onActive()
        ExchangeRateManager(EventBus.getDefault())
                .loadRates()
    }
}