package ps.isel.ipl.pt.gabageapp.util.loader

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by goncalo on 25/05/2018.
 */
class Header(var name: String, var value: String): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(value)
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object CREATOR : Parcelable.Creator<Header> {
        override fun createFromParcel(parcel: Parcel): Header {
            return Header(parcel)
        }

        override fun newArray(size: Int): Array<Header?> {
            return arrayOfNulls(size)
        }
    }
}