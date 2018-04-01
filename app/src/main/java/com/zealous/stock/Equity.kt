package com.zealous.stock

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


const val FIELD_NAME = "name"

open class Equity(@PrimaryKey var symbol: String, var name: String, var change: String, var price: Int) : RealmObject(), Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt())

    constructor() : this("", "", "", 0)

    companion object CREATOR : Parcelable.Creator<Equity> {
        override fun createFromParcel(parcel: Parcel): Equity {
            return Equity(parcel)
        }

        override fun newArray(size: Int): Array<Equity?> {
            return arrayOfNulls(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(symbol)
        parcel.writeString(name)
        parcel.writeString(change)
        parcel.writeInt(price)
    }

    override fun describeContents(): Int {
        return 0
    }
}