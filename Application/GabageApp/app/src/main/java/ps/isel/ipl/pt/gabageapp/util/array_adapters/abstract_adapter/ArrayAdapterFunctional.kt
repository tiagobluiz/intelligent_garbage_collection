package ps.isel.ipl.pt.gabageapp.util.array_adapters.abstract_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

/**
 * Created by Goncalo on 25/10/2017.
 */

abstract class ArrayAdapterFunctional<T>(internal var context: Context, val resourceId: Int,
                                val items: ArrayList<T>) : ArrayAdapter<T>(context, resourceId, items){

    override final fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var value : T = items[position]
        val cacheView : View

        if(convertView== null) {
            var inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            cacheView = inflator.inflate(resourceId, null)
        }
        else {
            cacheView = convertView
        }

        return action(value,cacheView)
    }

    fun addElements(elemets : List<T>){
        items.addAll(elemets)
    }

    abstract fun action(item :T,view :View):View
}
