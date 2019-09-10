package ps.isel.ipl.pt.gabageapp.util.array_adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.View
import kotlinx.android.synthetic.main.collect_zone_item.view.*
import ps.isel.ipl.pt.gabageapp.CollectZoneActivity
import ps.isel.ipl.pt.gabageapp.CollectZoneActivity.Companion.GET_COLLECTZONE
import ps.isel.ipl.pt.gabageapp.model.CollectZoneItem
import ps.isel.ipl.pt.gabageapp.util.array_adapters.abstract_adapter.ArrayAdapterFunctional
import java.util.ArrayList

/**
 * Created by goncalo on 22/04/2018.
 */
class ArrayAdapterCollectZone(internal var context1 : Context, val resourceId1: Int,
                              var items1 : ArrayList<CollectZoneItem>) : ArrayAdapterFunctional<CollectZoneItem>(context1,resourceId1,items1) {

    override fun action(item: CollectZoneItem, view: View): View {
        view.collect_zone_text.text = ""+item.id;
        view.latitude_text.text = "${item.location.latitude}|${item.location.longitude}"
        view.collect_zone_item_header.setBackgroundColor(if(item.active) Color.parseColor("#8BC34A") else Color.parseColor("#F44336"))

        view.collect_zone_details_button.setOnClickListener {
            var intent = Intent(context1, CollectZoneActivity::class.java)
            intent.putExtra(GET_COLLECTZONE,item.self)
            ContextCompat.startActivity(context1, intent, null)
        }
        return view
    }

}