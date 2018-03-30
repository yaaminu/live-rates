package com.zealous.stock

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


const val FIELD_NAME = "name"

open class Equity(@PrimaryKey var symbol: String, var name: String, var change: String, var price: Int) : RealmObject() {

    constructor() : this("", "", "", 0)
}