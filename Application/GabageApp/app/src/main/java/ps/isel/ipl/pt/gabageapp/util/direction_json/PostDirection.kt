package ps.isel.ipl.pt.gabageapp.util.direction_json

import com.google.android.gms.maps.model.LatLng

/**
 * Created by goncalo on 19/04/2018.
 */
interface PostDirection {

    fun onDirectionFinderSuccess(markers: List<LatLng>, route: List<LatLng>)
}