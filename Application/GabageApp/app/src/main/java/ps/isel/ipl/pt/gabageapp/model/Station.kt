package ps.isel.ipl.pt.gabageapp.model

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

/**
 * Created by goncalo on 24/04/2018.
 */
class Station(val id: Int, val location: LatLng, val type: String, val stationName: String, val self: String) : Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readParcelable(LatLng::class.java.classLoader),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeValue(location)
        dest.writeString(type)
        dest.writeString(stationName)
        dest.writeString(self)
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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