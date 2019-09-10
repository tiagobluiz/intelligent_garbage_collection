package ps.isel.ipl.pt.gabageapp.util.loader.json_home

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by goncalo on 01/07/2018.
 */
class Hints(val allow: Array<String>, val formats: Formats, val acceptPost: Array<String>): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.createStringArray(),
            parcel.readValue(Formats::class.java.classLoader) as Formats,
            parcel.createStringArray()) {
    }
    constructor(): this(
            arrayOf(""),
            Formats(),
            arrayOf("")
            ){

    }
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeStringArray(allow)
        dest.writeValue(formats)
        dest.writeStringArray(acceptPost)
    }

    override fun describeContents(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object CREATOR : Parcelable.Creator<Hints> {
        override fun createFromParcel(parcel: Parcel): Hints {
            return Hints(parcel)
        }

        override fun newArray(size: Int): Array<Hints?> {
            return arrayOfNulls(size)
        }
    }
}