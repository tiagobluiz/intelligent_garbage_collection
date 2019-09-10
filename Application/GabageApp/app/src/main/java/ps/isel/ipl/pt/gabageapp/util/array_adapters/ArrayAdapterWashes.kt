package ps.isel.ipl.pt.gabageapp.util.array_adapters

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.wash_item.view.*
import ps.isel.ipl.pt.gabageapp.model.Wash
import ps.isel.ipl.pt.gabageapp.util.array_adapters.abstract_adapter.ArrayAdapterFunctional

/**
 * Created by goncalo on 03/06/2018.
 */
class ArrayAdapterWashes(context: Context, resourceId: Int, items: ArrayList<Wash>) : ArrayAdapterFunctional<Wash>(context, resourceId, items) {
    override fun action(item: Wash, view: View): View {
        view.wash_date_text.text = item.date
        return view
    }
}