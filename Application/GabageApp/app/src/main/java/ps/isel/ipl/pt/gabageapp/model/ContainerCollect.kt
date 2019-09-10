package ps.isel.ipl.pt.gabageapp.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by goncalo on 23/06/2018.
 */
class ContainerCollect(val containerId : Int, val collecttDate: String): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString()) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(containerId)
        dest.writeString(collecttDate)
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object CREATOR : Parcelable.Creator<ContainerCollect> {
        override fun createFromParcel(parcel: Parcel): ContainerCollect {
            return ContainerCollect(parcel)
        }

        override fun newArray(size: Int): Array<ContainerCollect?> {
            return arrayOfNulls(size)
        }
    }
}