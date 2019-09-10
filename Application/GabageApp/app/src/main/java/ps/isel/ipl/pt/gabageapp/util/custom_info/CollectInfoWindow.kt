package ps.isel.ipl.pt.gabageapp.util.custom_info

import android.content.Context
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import android.app.Activity
import kotlinx.android.synthetic.main.custom_window_marker.view.*
import ps.isel.ipl.pt.gabageapp.R


/**
 * Created by goncalo on 26/06/2018.
 */
class CollectInfoWindow(val context : Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(marker: Marker?): View? {
        if(marker!=null) {
            val view = (context as Activity).layoutInflater
                    .inflate(R.layout.custom_window_marker, null)
            if(marker.tag != null){
                val container = marker.tag as CustomInfo
                view.marker_collect_zone_id_text.text = "${container.collectZoneId}"
                return view
            }
        }
        return null
    }

    override fun getInfoWindow(marker: Marker?): View? {
        return null
    }
}