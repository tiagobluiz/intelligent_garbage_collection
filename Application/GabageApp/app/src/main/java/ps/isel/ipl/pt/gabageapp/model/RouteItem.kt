package ps.isel.ipl.pt.gabageapp.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by goncalo on 13/04/2018.
 */
class RouteItem(val routId : Int, val startPoint: Location, val endPoint: Location, val enable: Boolean, val self: String):Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readValue(Location::class.java.classLoader)as Location,
            parcel.readValue(Location::class.java.classLoader)as Location,
            parcel.readValue(Boolean::class.java.classLoader) as Boolean,
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(routId)
        parcel.writeValue(startPoint)
        parcel.writeValue(endPoint)
        parcel.writeValue(enable)
        parcel.writeString(self)
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