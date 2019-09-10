package ps.isel.ipl.pt.gabageapp.model

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

/**
 * Created by goncalo on 24/04/2018.
 */
class Landfill(val id:Int, val location: LatLng): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readParcelable(LatLng::class.java.classLoader)) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeValue(location)
    }

    override fun describeContents(): Int {
       return 0
    }

    companion object CREATOR : Parcelable.Creator<Landfill> {
        override fun createFromParcel(parcel: Parcel): Landfill {
            return Landfill(parcel)
        }

        override fun newArray(size: Int): Array<Landfill?> {
            return arrayOfNulls(size)
        }
    }
}