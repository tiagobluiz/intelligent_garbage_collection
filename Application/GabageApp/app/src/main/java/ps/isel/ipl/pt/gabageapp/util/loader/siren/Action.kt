package ps.isel.ipl.pt.gabageapp.util.loader.siren

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by goncalo on 26/05/2018.
 */
class Action(var name: String, var title: String, var method: String, var href: String, var type: String, val fields: ArrayList<Field>) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readArrayList(Field::class.java.classLoader)as ArrayList<Field>) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        if(name == null)
            name = ""
        if(title == null)
            title = ""
        if(method == null)
            method = ""
        if(href == null)
            href = ""
        if(type == null)
            type = ""
        dest.writeString(name)
        dest.writeString(title)
        dest.writeString(method)
        dest.writeString(href)
        dest.writeString(type)
        dest.writeList(fields)
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object CREATOR : Parcelable.Creator<Action> {
        override fun createFromParcel(parcel: Parcel): Action {
            return Action(parcel)
        }

        override fun newArray(size: Int): Array<Action?> {
            return arrayOfNulls(size)
        }
    }
}