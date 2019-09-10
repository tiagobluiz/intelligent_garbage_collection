package ps.isel.ipl.pt.gabageapp.util.array_adapters

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.station_item.view.*
import ps.isel.ipl.pt.gabageapp.model.Station
import ps.isel.ipl.pt.gabageapp.util.array_adapters.abstract_adapter.ArrayAdapterFunctional
import java.util.ArrayList

/**
 * Created by goncalo on 24/04/2018.
 */
class ArrayAdapterStation (internal var context1 : Context, val resourceId1: Int,
                           var items1 : ArrayList<Station>) : ArrayAdapterFunctional<Station>(context1,resourceId1,items1) {

    override fun action(item: Station, view: View): View {
        view.station_id_text.text = ""+item.id
        view.location_text.text = "${item.location.latitude}|${item.location.longitude}"
        view.type_station_text.text = item.type
        return view
    }
}