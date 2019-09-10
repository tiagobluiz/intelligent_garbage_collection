package ps.isel.ipl.pt.gabageapp.util.array_adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.option_item.view.*
import ps.isel.ipl.pt.gabageapp.util.array_adapters.abstract_adapter.ArrayAdapterFunctional
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Option
import java.util.ArrayList

/**
 * Created by goncalo on 18/06/2018.
 */
class ArrayAdapterOptions(internal var context1 : Context, val resourceId1: Int,
                          var items1 : ArrayList<Option>) : ArrayAdapterFunctional<Option>(context1,resourceId1,items1) {
    override fun action(item: Option, view: View): View {
        view.option.text = item.text
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getDropDownView(position, convertView, parent)
        view.option.text = items1[position].text
        return view
    }
}