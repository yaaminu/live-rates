package com.zealous.equity

import io.realm.Realm


fun Realm.transaction(transaction: Realm.() -> Unit) {
    try {
        beginTransaction()
        transaction()
        commitTransaction()
    } catch (e: Exception) {
        cancelTransaction()
        throw e
    }
}