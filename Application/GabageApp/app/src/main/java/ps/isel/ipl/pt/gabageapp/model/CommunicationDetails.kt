package ps.isel.ipl.pt.gabageapp.model

import android.os.Parcel
import android.os.Parcelable
import android.view.View

/**
 * Created by goncalo on 10/07/2018.
 */
class CommunicationDetails(val configurationId: Int, val communicationId: Int, val communicationDesignation: String, val value: String, val self: String, var delete :View.OnClickListener): Parcelable {


    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(View.OnClickListener::class.java.classLoader) as View.OnClickListener) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(configurationId)
        parcel.writeInt(communicationId)
        parcel.writeString(communicationDesignation)
        parcel.writeString(value)
        parcel.writeString(self)
        parcel.writeValue(delete)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CommunicationDetails> {
        override fun createFromParcel(parcel: Parcel): CommunicationDetails {
            return CommunicationDetails(parcel)
        }

        override fun newArray(size: Int): Array<CommunicationDetails?> {
            return arrayOfNulls(size)
        }
    }
}