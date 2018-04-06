package com.zealous.ui

import com.zealous.stock.Equity
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

/**
 *  by yaaminu on 4/6/18.
 */
class WatchedStock(realm: Realm) : BaseRealmLiveData<Equity>(realm) {

    override fun load(): RealmResults<Equity> {
        return realm.where(Equity::class.java)
                .equalTo("isFavorite", true)
                .findAllSortedAsync("symbol", Sort.ASCENDING)
    }
}