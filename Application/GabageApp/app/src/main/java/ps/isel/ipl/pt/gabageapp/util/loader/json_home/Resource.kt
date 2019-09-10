package ps.isel.ipl.pt.gabageapp.util.loader.json_home

import android.os.Parcel
import android.os.Parcelable

class Resource(val href: String = "", val hrefTemplate: String= "", val hints: Hints = Hints()): Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString(), parcel.readString(), parcel.readValue(Hints::class.java.classLoader) as Hints) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(href)
        dest.writeString(hrefTemplate)
        dest.writeValue(hints)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Resource> {
        override fun createFromParcel(parcel: Parcel): Resource {
            return Resource(parcel)
        }

        override fun newArray(size: Int): Array<Resource?> {
            return arrayOfNulls(size)
        }
    }
}