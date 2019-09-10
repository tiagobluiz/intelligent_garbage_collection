package ps.isel.ipl.pt.gabageapp.util.direction_json

import android.os.AsyncTask
import com.androidnetworking.AndroidNetworking
import com.google.android.gms.maps.model.LatLng
import ps.isel.ipl.pt.gabageapp.util.direction_json.json_obj.DirectionMap
import ps.isel.ipl.pt.gabageapp.util.direction_json.json_obj.ResultDirection
import java.util.ArrayList

/**
 * Created by goncalo on 19/04/2018.
 */

class DirectionRequest(val handel : PostDirection) : AsyncTask<List<LatLng>, Void, ResultDirection>() {

    private val DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?origin=%s&destination=%s&waypoints=%s&key=%s"
    private val GOOGLE_API_KEY = "AIzaSyBhArpTB_TEUglTx2UAwF7jOxGJlc3ngC8"
    private val WAY_POITS = "via:%s"

    override fun doInBackground(vararg params: List<LatLng>): ResultDirection? {
        var route = params.first();
        var origin = route.first()
        var destination = route.last();
        var middlePoints : String =""
        route.forEachIndexed { index, latLng ->
            if(index >=1 && index <= route.lastIndex-1)
                middlePoints+=String.format(WAY_POITS,"${latLng.latitude},${latLng.longitude}")
                if(index != route.lastIndex-1)
                    middlePoints+="|"
        }
        val url = String.format(DIRECTION_URL_API,"${origin.latitude},${origin.longitude}","${destination.latitude},${destination.longitude}",middlePoints,GOOGLE_API_KEY)
        val request = AndroidNetworking.get(url).build().executeForObject(DirectionMap::class.java)
        if(request.isSuccess){
            var result = request.result as DirectionMap
            if(result.status.equals("OK")){
                var route = result.routes.first()
                var leg = route.legs.first()
                var markers = ArrayList<LatLng>()
                markers.add(LatLng(leg.start_location.lat, leg.start_location.lng))
                val waypoints = leg.via_waypoint.map { LatLng(it.location.lat, it.location.lng) }
                markers.addAll(waypoints)
                markers.add(LatLng(leg.end_location.lat, leg.end_location.lng))
                return ResultDirection(markers,decodePolyLine(route.overview_polyline.points))
            }
        }
        return null;
    }

    override fun onPostExecute(result: ResultDirection?) {
        if(result != null){
            handel.onDirectionFinderSuccess(result.marker,result.route);
        }
    }

    private fun decodePolyLine(poly: String): List<LatLng> {
        val len = poly.length
        var index = 0
        val decoded = ArrayList<LatLng>()
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = poly[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = poly[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            decoded.add(LatLng(
                    lat / 100000.0, lng / 100000.0
            ))
        }

        return decoded
    }
}
