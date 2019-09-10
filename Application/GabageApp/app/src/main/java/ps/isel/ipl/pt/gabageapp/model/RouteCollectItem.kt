package ps.isel.ipl.pt.gabageapp.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by goncalo on 04/06/2018.
 */
class RouteCollectItem(val finishDate: String, val startdate: String, val routeId: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeString(finishDate)
        parcel.writeString(startdate)
        parcel.writeInt(routeId)
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object CREATOR : Parcelable.Creator<RouteCollectItem> {
        override fun createFromParcel(parcel: Parcel): RouteCollectItem {
            return RouteCollectItem(parcel)
        }

        override fun newArray(size: Int): Array<RouteCollectItem?> {
            return arrayOfNulls(size)
        }
    }
}