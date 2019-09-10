package ps.isel.ipl.pt.gabageapp.util.array_adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.View
import kotlinx.android.synthetic.main.container_item.view.*
import ps.isel.ipl.pt.gabageapp.ContainerActivity
import ps.isel.ipl.pt.gabageapp.ContainerActivity.Companion.GET_CONTAINER
import ps.isel.ipl.pt.gabageapp.model.ContainerItem
import ps.isel.ipl.pt.gabageapp.util.array_adapters.abstract_adapter.ArrayAdapterFunctional
import java.util.ArrayList

/**
 * Created by goncalo on 24/04/2018.
 */
class ArrayAdapterCollectZoneContainers(internal var context1 : Context, val resourceId1: Int,
                                        var items1 : ArrayList<ContainerItem>) : ArrayAdapterFunctional<ContainerItem>(context1,resourceId1,items1) {
    override fun action(item: ContainerItem, view: View): View {
        view.container_id_text.text = ""+item.id
        view.battery_level_text.text = ""+item.battery
        view.temperature_level_text.text = ""+item.temperature+"ºC"
        view.occupancy_level_text.text = ""+item.occupation+"%"
        view.latitude_text.text = "${item.location.latitude}|${item.location.longitude}"
        view.collect_zone_container_id_label.visibility = View.INVISIBLE
        view.collect_zone_container_id_text.visibility = View.INVISIBLE
        view.container_item_header.setBackgroundColor(if(item.active) Color.parseColor("#8BC34A") else Color.parseColor("#F44336"))

        view.container_details_button.setOnClickListener {
            var intent = Intent(context1, ContainerActivity::class.java)
            intent.putExtra(GET_CONTAINER, item.self)
            ContextCompat.startActivity(context1, intent, null)
        }
        return view
    }
}