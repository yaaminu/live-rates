package com.zealous.ui

import android.arch.lifecycle.LiveData
import com.zealous.exchangeRates.ExchangeRate
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort

class WatchedCurrencies(private val realm: Realm) : LiveData<List<ExchangeRate>>() {
    private var watched: RealmResults<ExchangeRate>? = null

    private val listener = RealmChangeListener<RealmResults<ExchangeRate>> {
        value = it
    }

    override fun getValue(): List<ExchangeRate>? {
        if (watched == null) {
            loadWatched()
        }
        return watched
    }

    override fun onActive() {
        super.onActive()
        loadWatched()
        watched!!.addChangeListener(listener)
    }

    override fun onInactive() {
        watched?.removeChangeListener(listener)
        watched = null
        super.onInactive()
    }

    private fun loadWatched() {
        watched = watched ?: realm.where(ExchangeRate::class.java)
                .equalTo(ExchangeRate.FIELD_WATCHING, 1)
                .findAllSortedAsync(ExchangeRate.FIELD_CURRENCY_NAME, Sort.ASCENDING)
    }
}