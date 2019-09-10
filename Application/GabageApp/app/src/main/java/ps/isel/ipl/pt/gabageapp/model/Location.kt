package ps.isel.ipl.pt.gabageapp.model

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

/**
 * Created by goncalo on 13/04/2018.
 */
class Location(val latlng: LatLng, val locationName: String): Parcelable {
    constructor(parcel: Parcel) : this(
            LatLng(parcel.readDouble(),parcel.readDouble()),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latlng.latitude)
        parcel.writeDouble(latlng.longitude)
        parcel.writeString(locationName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Location> {
        override fun createFromParcel(parcel: Parcel): Location {
            return Location(parcel)
        }

        override fun newArray(size: Int): Array<Location?> {
            return arrayOfNulls(size)
        }
    }
}