package ps.isel.ipl.pt.gabageapp.util.array_adapters

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.truck_item.view.*
import ps.isel.ipl.pt.gabageapp.model.Truck
import ps.isel.ipl.pt.gabageapp.util.array_adapters.abstract_adapter.ArrayAdapterFunctional

/**
 * Created by goncalo on 20/04/2018.
 */
class ArrayAdapterTruck(var context1: Context, var resourceId1:Int,
                        var items1: ArrayList<Truck>) : ArrayAdapterFunctional<Truck>(context1, resourceId1, items1) {
    override fun action(item: Truck, view: View): View {
        view.truck_id_text.text = item.id
        return view
    }
}