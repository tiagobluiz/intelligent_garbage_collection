package ps.isel.ipl.pt.gabageapp.model

import com.google.android.gms.maps.model.LatLng

/**
 * Created by goncalo on 05/05/2018.
 */
class ContainerDetails(val id: Int, val temperature: Int, val occupation: Int, val battery: Int, val height: Int, val location: LatLng, val collectZoneId: Int, val numWash: Int, val numCollect: Int, val type: String, val iotId: String,val configurationId: Int, val active: Boolean, val washesURL: String, val collectsURL: String, val configurationsURL: String){
}