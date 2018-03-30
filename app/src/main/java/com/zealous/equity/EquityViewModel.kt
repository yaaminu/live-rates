package com.zealous.equity

import android.arch.lifecycle.ViewModel
import android.support.annotation.MainThread
import com.zealous.stock.Equity
import com.zealous.stock.FIELD_NAME
import com.zealous.utils.TaskManager
import com.zealous.utils.ThreadUtils
import io.realm.Realm
import io.realm.RealmResults


class EquityViewModel : ViewModel() {
    private var realm: Realm = Realm.getDefaultInstance()
    private var equities: RealmResults<Equity>? = null

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

    override fun onCleared() {
        realm.close()
        super.onCleared()
    }

    private fun createMockData() {
        TaskManager.executeNow({
            Realm.getDefaultInstance()
                    .use { realm ->
                        realm.transaction {
                            for (i in 1..100) {
                                copyToRealmOrUpdate(Equity("sym $i", "name $i", (if(i % 2 == 0) "+" else "-") + "2.4", 10 * i))
                            }
                        }
                    }

        }, false)
    }
}
