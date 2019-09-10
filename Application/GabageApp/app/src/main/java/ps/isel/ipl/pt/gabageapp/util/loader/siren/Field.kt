package ps.isel.ipl.pt.gabageapp.util.loader.siren

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by goncalo on 17/05/2018.
 */
class Field(var name: String, var type: String, var value: String, var title: String, var options: Array<Option>): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            arrayOf()) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        if(name == null)
            name = ""
        if(title == null)
            title = ""
        if(value == null)
            value = ""
        if(type == null)
            type = ""
        dest.writeString(name)
        dest.writeString(type)
        dest.writeString(value)
        dest.writeString(title)
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object CREATOR : Parcelable.Creator<Field> {
        override fun createFromParcel(parcel: Parcel): Field {
            return Field(parcel)
        }

        override fun newArray(size: Int): Array<Field?> {
            return arrayOfNulls(size)
        }
    }
}