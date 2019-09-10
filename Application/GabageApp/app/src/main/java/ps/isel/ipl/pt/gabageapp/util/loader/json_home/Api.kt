package ps.isel.ipl.pt.gabageapp.util.loader.json_home

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by goncalo on 25/05/2018.
 */
class Api(val title: String): Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object CREATOR : Parcelable.Creator<Api> {
        override fun createFromParcel(parcel: Parcel): Api {
            return Api(parcel)
        }

        override fun newArray(size: Int): Array<Api?> {
            return arrayOfNulls(size)
        }
    }
}