package ps.isel.ipl.pt.gabageapp.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by goncalo on 20/04/2018.
 */
class Truck(val id: String, val active: Boolean, val self: String): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readValue(Boolean::class.java.classLoader) as Boolean,
            parcel.readString()) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeValue(active)
        dest.writeString(self)
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object CREATOR : Parcelable.Creator<Truck> {
        override fun createFromParcel(parcel: Parcel): Truck {
            return Truck(parcel)
        }

        override fun newArray(size: Int): Array<Truck?> {
            return arrayOfNulls(size)
        }
    }
}