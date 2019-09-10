package ps.isel.ipl.pt.gabageapp.model

import com.google.android.gms.maps.model.LatLng

/**
 * Created by goncalo on 29/05/2018.
 */
class CollectZone(val collectZoneId: Int,
                  val routeId: Int,
                  val numContainers: Int,
                  val active: Boolean,
                  val location: LatLng,
                  val generalOccupation: Int,
                  val plasticOccupation: Int,
                  val paperOccupation: Int,
                  val glassOccupation: Int,
                  val containerUrl: String) {
}