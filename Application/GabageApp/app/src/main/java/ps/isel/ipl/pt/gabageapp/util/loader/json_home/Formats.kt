package ps.isel.ipl.pt.gabageapp.util.loader.json_home

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Created by goncalo on 01/07/2018.
 */
class Formats(@SerializedName("application/json")val json: ApllicationJsonCollectRoute = ApllicationJsonCollectRoute()): Parcelable {
    constructor(parcel: Parcel) : this(parcel.readValue(ApllicationJsonCollectRoute::class.java.classLoader) as ApllicationJsonCollectRoute) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(json)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Formats> {
        override fun createFromParcel(parcel: Parcel): Formats {
            return Formats(parcel)
        }

        override fun newArray(size: Int): Array<Formats?> {
            return arrayOfNulls(size)
        }
    }
}