package ps.isel.ipl.pt.gabageapp.util.loader

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by goncalo on 13/06/2018.
 */
class ErrorApi(val type: String, val title: String, val status: Int, val message: String, val detail: String): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(type)
        dest.writeString(title)
        dest.writeInt(status)
        dest.writeString(message)
        dest.writeString(detail)
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object CREATOR : Parcelable.Creator<ErrorApi> {
        override fun createFromParcel(parcel: Parcel): ErrorApi {
            return ErrorApi(parcel)
        }

        override fun newArray(size: Int): Array<ErrorApi?> {
            return arrayOfNulls(size)
        }
    }
}