package ps.isel.ipl.pt.gabageapp.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by goncalo on 06/05/2018.
 */
class Wash(val containeId: Int, val date: String) : Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString()) {
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object CREATOR : Parcelable.Creator<Wash> {
        override fun createFromParcel(parcel: Parcel): Wash {
            return Wash(parcel)
        }

        override fun newArray(size: Int): Array<Wash?> {
            return arrayOfNulls(size)
        }
    }
}