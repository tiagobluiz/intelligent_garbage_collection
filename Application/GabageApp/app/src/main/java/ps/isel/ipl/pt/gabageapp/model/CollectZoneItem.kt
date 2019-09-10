package ps.isel.ipl.pt.gabageapp.model

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

/**
 * Created by goncalo on 22/04/2018.
 */
class CollectZoneItem(val id:Int, val active: Boolean, val location: LatLng, val occupancy: Float, val self: String, var collectURI: String?, var washURI: String?): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readByte() != 0.toByte(),
            parcel.readParcelable(LatLng::class.java.classLoader),
            parcel.readFloat(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
        if(collectURI.equals(""))
            collectURI=null
        if(washURI.equals(""))
            washURI = null
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeByte(if (active) 1 else 0)
        parcel.writeParcelable(location, flags)
        parcel.writeFloat(occupancy)
        parcel.writeString(self)
        if(collectURI==null)
            collectURI = ""
        parcel.writeString(collectURI)
        if(washURI==null)
            washURI = ""
        parcel.writeString(washURI)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CollectZoneItem> {
        override fun createFromParcel(parcel: Parcel): CollectZoneItem {
            return CollectZoneItem(parcel)
        }

        override fun newArray(size: Int): Array<CollectZoneItem?> {
            return arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if(other is CollectZoneItem)
            return (other as CollectZoneItem).id.equals(this.id)
        return false
    }

    override fun hashCode(): Int {
        return this.id
    }

    override fun toString(): String {
        return "${this.id}"
    }
}