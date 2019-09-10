package ps.isel.ipl.pt.gabageapp.service_web_api.dto

/**
 * Created by goncalo on 29/05/2018.
 */
class CollectZoneDto(val collectZoneId: Int,
                     val routeId: Int,
                     val pickOrder: Long,
                     val active: String,
                     val numContainers: Int,
                     val latitude: Double,
                     val longitude: Double,
                     val generalOccupation: Int,
                     val plasticOccupation: Int,
                     val paperOccupation: Int,
                     val glassOccupation: Int) {
}