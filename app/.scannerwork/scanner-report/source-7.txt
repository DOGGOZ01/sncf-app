package com.example.sncf.classes

import android.os.Parcel
import android.os.Parcelable
import java.util.ArrayList

data class Train(
    val num: String,
    val libelle: String,
    val type: TypeTrain,
    val localHour: String,
    val localMinute: String,
    val numJourney: String ) : Parcelable {


    var stops = ArrayList<Stop>()


    var from: Stop? = null
    var to: Stop? = null

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        TypeTrain.valueOf(parcel.readString()!!),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
        from = parcel.readParcelable(Stop::class.java.classLoader)
        to = parcel.readParcelable(Stop::class.java.classLoader)
        parcel.readList(stops, Stop::class.java.classLoader)
    }

    fun addStop(stop: Stop, isDepartureStation: Boolean, isArrivalStation: Boolean) {
        stops.add(stop)
        if (isDepartureStation) from = stop
        if (isArrivalStation) to = stop
    }

    override fun toString(): String {
        return from!!.getDeparture()+ " - " + this.libelle + "\n" + type.name + " - " + num
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(num)
        parcel.writeString(libelle)
        parcel.writeString(type.name)
        parcel.writeString(localHour)
        parcel.writeString(localMinute)
        parcel.writeString(numJourney)
        parcel.writeParcelable(from, flags)
        parcel.writeParcelable(to, flags)
        parcel.writeList(stops)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Train> {
        override fun createFromParcel(parcel: Parcel): Train {
            return Train(parcel)
        }

        override fun newArray(size: Int): Array<Train?> {
            return arrayOfNulls(size)
        }
    }
}
