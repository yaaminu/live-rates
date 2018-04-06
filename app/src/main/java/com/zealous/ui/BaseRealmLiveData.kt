package com.zealous.ui

import android.arch.lifecycle.LiveData
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmObject
import io.realm.RealmResults

/**
 * Created by yaaminu on 4/6/18.
 */
abstract class BaseRealmLiveData<T : RealmObject>(protected val realm: Realm) : LiveData<List<T>>() {
    private var watched: RealmResults<T>? = null

    private val listener = RealmChangeListener<RealmResults<T>> {
        value = it
    }

    override fun getValue(): List<T>? {
        if (watched == null) {
            watched = load()
        }
        return watched
    }

    override fun onActive() {
        super.onActive()
        watched = load()
        watched!!.addChangeListener(listener)
    }

    override fun onInactive() {
        watched?.removeChangeListener(listener)
        watched = null
        super.onInactive()
    }

    abstract fun load(): RealmResults<T>


}