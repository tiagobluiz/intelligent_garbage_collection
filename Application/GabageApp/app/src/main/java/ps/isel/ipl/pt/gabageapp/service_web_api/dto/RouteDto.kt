package ps.isel.ipl.pt.gabageapp.service_web_api.dto

/**
 * Created by goncalo on 09/04/2018.
 */

class RouteDto(val numContainers: Int, val numCollectZones: Int, val numCollects: Int, val routeId: Int, val startPointStationName : String, val finishPointStationName: String, val active: String, val startPointLatitude: Double, val startPointLongitude: Double, val finishPointLatitude: Double, val finishPointLongitude: Double) {
}