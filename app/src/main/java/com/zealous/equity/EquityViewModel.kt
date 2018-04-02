package com.zealous.equity

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.support.annotation.MainThread
import com.zealous.stock.Equity
import com.zealous.stock.FIELD_NAME
import com.zealous.utils.PLog
import com.zealous.utils.TaskManager
import com.zealous.utils.ThreadUtils
import io.realm.Realm
import io.realm.RealmResults
import io.realm.exceptions.RealmPrimaryKeyConstraintException


class EquityViewModel : ViewModel() {
    private var realm: Realm = Realm.getDefaultInstance()
    private var equities: RealmResults<Equity>? = null
    private var favorites: MutableLiveData<RealmResults<Equity>> = MutableLiveData()


    private var favoriteChangeListener = { realmResults: RealmResults<Equity> ->
        favorites.value = realmResults
    }

    init {
        createMockData()
    }

    @MainThread
    fun getEquities(): RealmResults<Equity> {
        ThreadUtils.ensureMain()
        equities = equities ?: realm.where(Equity::class.java)
                .findAllSorted(FIELD_NAME)
        return equities!!
    }

    fun getWatchedEquities(): LiveData<RealmResults<Equity>> {
        ThreadUtils.ensureMain()
        if (favorites.value == null) {
            favorites.value = realm.where(Equity::class.java)
                    .equalTo("isFavorite", true)
                    .findAllSortedAsync("name")
        }
        favorites.value!!.addChangeListener(favoriteChangeListener)
        return favorites
    }

    override fun onCleared() {
        favorites.value?.removeChangeListener(favoriteChangeListener)
        realm.close()
        super.onCleared()
    }

    private fun createMockData() {
        TaskManager.executeNow({
            Realm.getDefaultInstance()
                    .use { realm ->
                        realm.transaction {
                            for (i in 1..100) {
                                val equity = Equity("sym $i".toUpperCase(), "name $i", (if (i % 2 == 0) "+" else "-") + "2.4(3.2%)",
                                        "${10 * i}.00", 12222, 2333.toDouble(), 2300.toDouble(), 232323.toDouble(), 23323.0, 2230, false)
                                try {
                                    copyToRealm(equity)
                                } catch (e: RealmPrimaryKeyConstraintException) {
                                    PLog.e(TAG, e.message)
                                }
                            }
                        }
                    }

        }, false)
    }

}
