package ps.isel.ipl.pt.gabageapp.util.array_adapters

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.landfill_item.view.*
import ps.isel.ipl.pt.gabageapp.model.Landfill
import ps.isel.ipl.pt.gabageapp.util.array_adapters.abstract_adapter.ArrayAdapterFunctional
import java.util.ArrayList

/**
 * Created by goncalo on 24/04/2018.
 */
class ArrayAdapterLandFill(internal var context1 : Context, val resourceId1: Int,
                           var items1 : ArrayList<Landfill>) : ArrayAdapterFunctional<Landfill>(context1,resourceId1,items1) {

    override fun action(item: Landfill, view: View): View {
        view.landfill_id_text.text = ""+item.id
        view.location_text.text = "${item.location.latitude}|${item.location.longitude}"
        return view
    }
}