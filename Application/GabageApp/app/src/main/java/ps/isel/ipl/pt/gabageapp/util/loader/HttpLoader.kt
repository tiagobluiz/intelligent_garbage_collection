package ps.isel.ipl.pt.gabageapp.util.loader

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by goncalo on 25/05/2018.
 */
class HttpLoader(var url: String, var headers : Array<Header>): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readValue(Array<Header>::class.java.classLoader)as Array<Header>) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(url)
        dest.writeValue(headers)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HttpLoader> {
        override fun createFromParcel(parcel: Parcel): HttpLoader {
            return HttpLoader(parcel)
        }

        override fun newArray(size: Int): Array<HttpLoader?> {
            return arrayOfNulls(size)
        }
    }
}