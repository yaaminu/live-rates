package com.zealous.ui

import com.zealous.exchangeRates.ExchangeRate
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

class WatchedCurrencies(realm: Realm) : BaseRealmLiveData<ExchangeRate>(realm) {
    override fun load(): RealmResults<ExchangeRate> {
        return realm.where(ExchangeRate::class.java)
                .equalTo(ExchangeRate.FIELD_WATCHING, 1)
                .findAllSortedAsync(ExchangeRate.FIELD_CURRENCY_NAME, Sort.ASCENDING)
    }
}