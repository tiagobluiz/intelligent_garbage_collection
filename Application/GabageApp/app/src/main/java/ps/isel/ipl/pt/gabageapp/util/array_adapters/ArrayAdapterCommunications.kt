package ps.isel.ipl.pt.gabageapp.util.array_adapters

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.communication_item.view.*
import ps.isel.ipl.pt.gabageapp.model.CommunicationDetails
import ps.isel.ipl.pt.gabageapp.util.array_adapters.abstract_adapter.ArrayAdapterFunctional
import java.util.ArrayList

/**
 * Created by goncalo on 10/07/2018.
 */
class ArrayAdapterCommunications(internal var context1 : Context, val resourceId1: Int,
                                 var items1 : ArrayList<CommunicationDetails>): ArrayAdapterFunctional<CommunicationDetails>(context1,resourceId1,items1){
    override fun action(details: CommunicationDetails, view: View): View {
        view.communication_id_text.text = "${details.communicationId}"
        view.communication_name_text.text = details.communicationDesignation
        view.communication_value_text.text = details.value
        view.communication_item_delete_button.setOnClickListener(details.delete)
        return view
    }
}