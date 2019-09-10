package ps.isel.ipl.pt.gabageapp.util.custom_info

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.custom_window_marker.view.*
import ps.isel.ipl.pt.gabageapp.R
import ps.isel.ipl.pt.gabageapp.model.CollectZoneItem

/**
 * Created by goncalo on 13/07/2018.
 */
class CollectZonesInfoWindow(val context : Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(marker: Marker?): View? {
        if(marker!=null) {
            val view = (context as Activity).layoutInflater
                    .inflate(R.layout.custom_window_marker, null)
            if(marker.tag != null){
                val collectZone = marker.tag as CollectZoneItem
                view.marker_collect_zone_id_text.text = "${collectZone.id}"
                return view
            }
        }
        return null
    }

    override fun getInfoWindow(marker: Marker?): View? {
        return null
    }
}