package ps.isel.ipl.pt.gabageapp.util.loader.json_home

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by goncalo on 25/05/2018.
 */
class HomeJson(val api: Api, val resources: Resources): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readParcelable(Api::class.java.classLoader),
            parcel.readParcelable(Resources::class.java.classLoader)) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(api, flags)
        parcel.writeParcelable(resources, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HomeJson> {
        override fun createFromParcel(parcel: Parcel): HomeJson {
            return HomeJson(parcel)
        }

        override fun newArray(size: Int): Array<HomeJson?> {
            return arrayOfNulls(size)
        }
    }
}