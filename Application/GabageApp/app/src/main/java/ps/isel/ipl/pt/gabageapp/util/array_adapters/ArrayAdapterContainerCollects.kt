package ps.isel.ipl.pt.gabageapp.util.array_adapters

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.wash_item.view.*
import ps.isel.ipl.pt.gabageapp.model.ContainerCollect
import ps.isel.ipl.pt.gabageapp.util.array_adapters.abstract_adapter.ArrayAdapterFunctional

/**
 * Created by goncalo on 23/06/2018.
 */
class ArrayAdapterContainerCollects (context: Context, resourceId: Int, items: ArrayList<ContainerCollect>) : ArrayAdapterFunctional<ContainerCollect>(context, resourceId, items) {
    override fun action(item: ContainerCollect, view: View): View {
        view.wash_date_text.text = item.collecttDate
        return view
    }
}