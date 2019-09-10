package ps.isel.ipl.pt.gabageapp.model

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

/**
 * Created by goncalo on 24/04/2018.
 */
class ContainerItem(val id: Int, val temperature: Int, val occupation: Int, val battery: Int, val location: LatLng, val collectZoneId: Int, val active: Boolean, val self: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readValue(LatLng::class.java.classLoader) as LatLng,
            parcel.readInt(),
            parcel.readValue(Boolean::class.java.classLoader) as Boolean,
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeInt(id)
        parcel.writeInt(temperature)
        parcel.writeInt(occupation)
        parcel.writeInt(battery)
        parcel.writeValue(location)
        parcel.writeInt(collectZoneId)
        parcel.writeValue(active)
        parcel.writeString(self)
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object CREATOR : Parcelable.Creator<ContainerItem> {
        override fun createFromParcel(parcel: Parcel): ContainerItem {
            return ContainerItem(parcel)
        }

        override fun newArray(size: Int): Array<ContainerItem?> {
            return arrayOfNulls(size)
        }
    }
}