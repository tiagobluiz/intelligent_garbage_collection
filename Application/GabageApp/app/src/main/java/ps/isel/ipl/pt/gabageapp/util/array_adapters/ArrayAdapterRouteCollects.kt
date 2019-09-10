package ps.isel.ipl.pt.gabageapp.util.array_adapters

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.collect_item.view.*
import ps.isel.ipl.pt.gabageapp.model.RouteCollectItem
import ps.isel.ipl.pt.gabageapp.util.array_adapters.abstract_adapter.ArrayAdapterFunctional

/**
 * Created by goncalo on 04/06/2018.
 */
class ArrayAdapterRouteCollects(context: Context, resourceId: Int, items: ArrayList<RouteCollectItem>) : ArrayAdapterFunctional<RouteCollectItem>(context, resourceId, items) {

    override fun action(item: RouteCollectItem, view: View): View {
        view.satrt_date_collect_text.text = item.startdate
        view.finish_date_collect_text.text = item.finishDate
        return view
    }
}