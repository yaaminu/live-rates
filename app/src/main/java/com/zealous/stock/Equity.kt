package com.zealous.stock

import android.os.Parcel
import android.os.Parcelable
import com.zealous.exchangeRates.ExchangeRate
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


const val FIELD_NAME = "name"

open class Equity(@PrimaryKey var symbol: String,
                  var name: String, var change: String,
                  var price: String, private var shares: Long,
                  var _24hrHi: Double, var _24hrLo: Double,
                  var marketCap: Double, var marketOpen: Double,
                  private var volume: Int,
                  var isFavorite: Boolean = false) : RealmObject(), Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(), parcel.readLong(),
            parcel.readDouble(), parcel.readDouble(), parcel.readDouble(), parcel.readDouble(), parcel.readInt(),
            parcel.readByte() == 1.toByte())

    constructor() : this("", "", "", "", 0L, 0.0, 0.0, 0.0, 0.0, 0, false)

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
        parcel.writeString(price)
        parcel.writeLong(shares)
        parcel.writeDouble(_24hrHi)
        parcel.writeDouble(_24hrLo)
        parcel.writeDouble(marketCap)
        parcel.writeDouble(marketOpen)
        parcel.writeInt(volume)
        parcel.writeByte(if (isFavorite) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun marketCapFormatted(): CharSequence? {
        return ExchangeRate.FORMAT.format(marketCap)
    }

    fun volumeFormated(): CharSequence? {
        return ExchangeRate.FORMAT.format(volume)
    }
}