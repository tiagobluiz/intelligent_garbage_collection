package ps.isel.ipl.pt.gabageapp.util.array_adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.content.ContextCompat.startActivity
import android.view.View
import kotlinx.android.synthetic.main.route_item.view.*
import ps.isel.ipl.pt.gabageapp.RouteActivity
import ps.isel.ipl.pt.gabageapp.RouteActivity.Companion.GET_ROUTE
import ps.isel.ipl.pt.gabageapp.model.RouteItem
import ps.isel.ipl.pt.gabageapp.util.array_adapters.abstract_adapter.ArrayAdapterFunctional
import java.util.*

/**
 * Created by goncalo on 13/04/2018.
 */
class ArrayAdapterRoute(internal var context1 : Context, val resourceId1: Int,
                        var items1 : ArrayList<RouteItem>) : ArrayAdapterFunctional<RouteItem>(context1,resourceId1,items1) {

    override fun action(item: RouteItem, view: View): View {
        view.router_id_text.text = ""+item.routId
        view.start_point_text.text = item.startPoint.locationName
        view.end_point_text.text = item.endPoint.locationName
        view.header_route.setBackgroundColor(if(item.enable) Color.parseColor("#8BC34A") else Color.parseColor("#F44336"))

        view.route_details_button.setOnClickListener {
            var intent = Intent(context1, RouteActivity::class.java)
            intent.putExtra(GET_ROUTE,item)
            startActivity(context1, intent, null)
        }

        return view
    }
}