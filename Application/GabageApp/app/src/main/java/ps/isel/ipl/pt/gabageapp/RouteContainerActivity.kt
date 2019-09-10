package ps.isel.ipl.pt.gabageapp

import kotlinx.android.synthetic.main.content_containers.*
import ps.isel.ipl.pt.gabageapp.model.ContainerItem
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterRouteContainer

/**
 * Created by goncalo on 24/04/2018.
 */
class RouteContainerActivity : ContainersActivity() {

    override fun inititialize(list: ArrayList<ContainerItem>) {
        containers_list_view.adapter = ArrayAdapterRouteContainer(this, R.layout.container_item, list)
        notifyChange()
    }

    override fun notifyChange() {
        (containers_list_view.adapter as ArrayAdapterRouteContainer).notifyDataSetChanged()
    }
}