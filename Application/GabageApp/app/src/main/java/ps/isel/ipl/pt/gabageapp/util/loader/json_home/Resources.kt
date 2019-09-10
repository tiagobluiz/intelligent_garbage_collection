package ps.isel.ipl.pt.gabageapp.util.loader.json_home

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


class Resources(@SerializedName("/rels/route-list")val getRoutes: Resource,
                @SerializedName("/rels/station-list")val getStations: Resource,
                @SerializedName("/rels/configuration-list")val getConfigurations: Resource,
                @SerializedName("/rels/communication-list")val getComminications: Resource,
                @SerializedName("/rels/truck-list")val getTrucks: Resource,
                @SerializedName("/rels/containers-in-range")val getOccupationRange: Resource,
                @SerializedName("/rels/employee")val getCurrentEmployee: Resource,
                @SerializedName("/rels/collect-route")val getCollectRoute: Resource,
                @SerializedName("/rels/collect-zones-in-range")val getCollectInRange: Resource): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readValue(Resource::class.java.classLoader) as Resource,
            parcel.readValue(Resource::class.java.classLoader) as Resource,
            parcel.readValue(Resource::class.java.classLoader) as Resource,
            parcel.readValue(Resource::class.java.classLoader) as Resource,
            parcel.readValue(Resource::class.java.classLoader) as Resource,
            parcel.readValue(Resource::class.java.classLoader) as Resource,
            parcel.readValue(Resource::class.java.classLoader) as Resource,
            parcel.readValue(Resource::class.java.classLoader) as Resource,
            parcel.readValue(Resource::class.java.classLoader) as Resource) {
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(getRoutes)
        dest.writeValue(getStations)
        dest.writeValue(getConfigurations)
        dest.writeValue(getComminications)
        dest.writeValue(getTrucks)
        dest.writeValue(getOccupationRange)
        dest.writeValue(getCurrentEmployee)
        dest.writeValue(getCollectRoute)
        dest.writeValue(getCollectInRange)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Resources> {
        override fun createFromParcel(parcel: Parcel): Resources {
            return Resources(parcel)
        }

        override fun newArray(size: Int): Array<Resources?> {
            return arrayOfNulls(size)
        }
    }
}
