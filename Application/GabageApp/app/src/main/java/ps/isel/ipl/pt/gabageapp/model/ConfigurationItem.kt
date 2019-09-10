package ps.isel.ipl.pt.gabageapp.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by goncalo on 13/07/2018.
 */
class ConfigurationItem(val configurationId: Int, val configurationName: String): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(configurationId)
        parcel.writeString(configurationName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ConfigurationItem> {
        override fun createFromParcel(parcel: Parcel): ConfigurationItem {
            return ConfigurationItem(parcel)
        }

        override fun newArray(size: Int): Array<ConfigurationItem?> {
            return arrayOfNulls(size)
        }
    }
}