package ps.isel.ipl.pt.gabageapp.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by goncalo on 28/05/2018.
 */
class Route(val routId : Int,
            val startPoint: Location,
            val endPoint: Location,
            val enable: Boolean,
            val numContainers: Int,
            val numCollectZones:Int,
            val numCollects: Int,
            val containersURL: String,
            val collecZonesURL: String,
            val collectsURL: String,
            val planURL: String,
            val dropZonesURL: String): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readValue(Location::class.java.classLoader)as Location,
            parcel.readValue(Location::class.java.classLoader)as Location,
            parcel.readValue(Boolean::class.java.classLoader) as Boolean,
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(routId)
        parcel.writeValue(startPoint)
        parcel.writeValue(endPoint)
        parcel.writeValue(enable)
        parcel.writeInt(numContainers)
        parcel.writeInt(numCollectZones)
        parcel.writeInt(numCollects)
        parcel.writeString(containersURL)
        parcel.writeString(collecZonesURL)
        parcel.writeString(collectsURL)
        parcel.writeString(planURL)
        parcel.writeString(dropZonesURL)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RouteItem> {
        override fun createFromParcel(parcel: Parcel): RouteItem {
            return RouteItem(parcel)
        }

        override fun newArray(size: Int): Array<RouteItem?> {
            return arrayOfNulls(size)
        }
    }
}