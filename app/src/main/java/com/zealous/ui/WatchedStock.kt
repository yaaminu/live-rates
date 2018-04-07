package com.zealous.ui

import com.zealous.stock.Equity
import com.zealous.utils.PLog
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 *  by yaaminu on 4/6/18.
 */
class WatchedStock(realm: Realm) : BaseRealmLiveData<Equity>(realm) {
    private val tag = "WatchedStock"
    private var subscription: Subscription? = null
    override fun load(): RealmResults<Equity> {
        return realm.where(Equity::class.java)
                .equalTo("isFavorite", true)
                .findAllSortedAsync("symbol", Sort.ASCENDING)
    }

    override fun onActive() {
        super.onActive()
        subscription = EquityRepository().loadStocks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    PLog.d(tag, "loaded stocks successfully")
                }) {
                    PLog.e(tag, it.message, it)
                }
    }

    override fun onInactive() {
        if (subscription != null && !subscription!!.isUnsubscribed) {
            subscription?.unsubscribe()
            subscription = null
        }
        super.onInactive()
    }
}