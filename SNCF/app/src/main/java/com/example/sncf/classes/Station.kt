package com.example.sncf.classes;

import android.os.Parcel
import android.os.Parcelable

class Station(
    val CODE_UIC: Int,
    val libelle: String,
    val long: Double,
    val lat: Double,
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() as String,
        parcel.readDouble(),
        parcel.readDouble()
    )


    override fun toString(): String {
        return this.libelle
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeInt(CODE_UIC)
        p0.writeString(libelle)
        p0.writeDouble(long)
        p0.writeDouble(lat)
    }

    companion object CREATOR : Parcelable.Creator<Station> {
        override fun createFromParcel(parcel: Parcel): Station {
            return Station(parcel)
        }

        override fun newArray(size: Int): Array<Station?> {
            return arrayOfNulls(size)
        }
    }
}