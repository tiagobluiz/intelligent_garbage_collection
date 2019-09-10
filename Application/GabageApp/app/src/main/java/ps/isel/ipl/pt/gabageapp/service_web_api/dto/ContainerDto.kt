package ps.isel.ipl.pt.gabageapp.service_web_api.dto

/**
 * Created by goncalo on 09/04/2018.
 */
class ContainerDto(val numCollects: Int,
                   val numWashes: Int,
                   val containerId: Int,
                   val collectZoneId: Int,
                   val configurationId: Int,
                   val latitude: Double,
                   val longitude: Double,
                   val height: Int,
                   val battery: Int,
                   val temperature: Int,
                   val occupation: Int,
                   val containerType: String,
                   val iotId: String,
                   val active: String,
                   val lastReadDate: String){

}