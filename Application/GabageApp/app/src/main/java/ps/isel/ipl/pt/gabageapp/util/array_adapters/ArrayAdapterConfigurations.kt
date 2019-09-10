package ps.isel.ipl.pt.gabageapp.util.array_adapters

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.configuration_item.view.*
import ps.isel.ipl.pt.gabageapp.model.ConfigurationItem
import ps.isel.ipl.pt.gabageapp.util.array_adapters.abstract_adapter.ArrayAdapterFunctional

/**
 * Created by goncalo on 13/07/2018.
 */
class ArrayAdapterConfigurations(context: Context, resourceId: Int, items: ArrayList<ConfigurationItem>)
    : ArrayAdapterFunctional<ConfigurationItem>(context, resourceId, items) {

    override fun action(item: ConfigurationItem, view: View): View {
        view.configuration_id_text.text = "${item.configurationId}"
        view.configuration_name_text .text = item.configurationName
        return view
    }
}