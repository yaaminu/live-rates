package com.zealous.equity

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.support.annotation.MainThread
import com.zealous.stock.Equity
import com.zealous.stock.FIELD_NAME
import com.zealous.utils.ThreadUtils
import io.realm.Realm
import io.realm.RealmResults


class EquityViewModel : ViewModel() {
    private var realm: Realm = Realm.getDefaultInstance()


    @MainThread
    fun getEquities(): LiveData<List<Equity>> {
        ThreadUtils.ensureMain()
        return equities
    }

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }


    private var equities: LiveData<List<Equity>> = object : MutableLiveData<List<Equity>>() {
        override fun onActive() {
            super.onActive()
            value = realm.where(Equity::class.java)
                    .findAllSortedAsync(FIELD_NAME)
            (value as RealmResults<Equity>?)?.addChangeListener { results ->
                value = results
            }
        }

        override fun onInactive() {
            if (value != null) {
                (value as RealmResults<Equity>).removeAllChangeListeners()
            }
            super.onInactive()
        }
    }

}
