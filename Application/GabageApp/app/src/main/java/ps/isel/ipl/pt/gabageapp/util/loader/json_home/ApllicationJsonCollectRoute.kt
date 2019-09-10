package ps.isel.ipl.pt.gabageapp.util.loader.json_home

import android.os.Parcel
import android.os.Parcelable
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Option
import java.lang.reflect.Type

/**
 * Created by goncalo on 01/07/2018.
 */
class ApllicationJsonCollectRoute(var truckPlate:String = " ", var startDate: String = " ", var finishDate: String = " ", val containerTypeOptions: ArrayList<Option> = arrayListOf<Option>()): Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readArrayList(Option::class.java.classLoader)as ArrayList<Option>) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        if(truckPlate == null)
            truckPlate = ""
        if(startDate == null)
            startDate = ""
        if(finishDate == null)
            finishDate = ""
        parcel.writeString(truckPlate)
        parcel.writeString(startDate)
        parcel.writeString(finishDate)
        parcel.writeList(containerTypeOptions)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ApllicationJsonCollectRoute> {
        override fun createFromParcel(parcel: Parcel): ApllicationJsonCollectRoute {
            return ApllicationJsonCollectRoute(parcel)
        }

        override fun newArray(size: Int): Array<ApllicationJsonCollectRoute?> {
            return arrayOfNulls(size)
        }
    }

}