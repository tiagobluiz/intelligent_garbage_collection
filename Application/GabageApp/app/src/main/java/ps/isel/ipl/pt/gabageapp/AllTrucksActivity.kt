package ps.isel.ipl.pt.gabageapp

import android.content.Loader
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_trucks.*
import kotlinx.android.synthetic.main.content_trucks.*
import ps.isel.ipl.pt.gabageapp.model.Truck
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterTruck
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.scroll.EndlessScrollListener

class AllTrucksActivity : AppCompatActivity(), LoaderCallbacksError<CollectionJson> {

    companion object {
        val GET_TRUCKS = "getTucks"
    }

    private val TAG = "TrucksListActivity"
    private val LOADER_ID = 1384
    private val REQUEST = "request"
    private val SAVE_LIST_STATE = "truck_list_state"
    private lateinit var scrollListener: EndlessScrollListener
    private var truckItemList: ArrayList<Truck> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trucks)
        setSupportActionBar(toolbar)
        Log.i(TAG, "onCreate")
        var url = intent.extras.get(GET_TRUCKS)

        scrollListener = object : EndlessScrollListener(20) {
            override fun onLoadMore(link: String): Boolean {
                Log.i(TAG, "New Link is load")
                var bundel = Bundle()
                var http = HttpLoader("${WasteManageApi.HOST_NAME_API}$link", arrayOf(Header("Accept","application/vnd.collection+json")))
                bundel.putParcelable(REQUEST,http)
                loaderManager.restartLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@AllTrucksActivity)
                return true
            }
        }

        var saved = savedInstanceState?.getParcelableArrayList<Truck>(SAVE_LIST_STATE)
        if(saved != null) {
            Log.i(TAG, "Reload list saved")
            truckItemList.addAll(0,saved)
        }else{
            Log.i(TAG, "Load frist time")
            var bundel = Bundle()
            var http = HttpLoader("${WasteManageApi.HOST_NAME_API}$url?showInactive=true", arrayOf(Header("Accept", "application/vnd.collection+json")))
            bundel.putParcelable(REQUEST, http)
            loaderManager.initLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@AllTrucksActivity)
        }

        truck_grid_view.adapter = ArrayAdapterTruck(this@AllTrucksActivity, R.layout.truck_item, truckItemList)
        (truck_grid_view.adapter as ArrayAdapterTruck).notifyDataSetChanged()
        truck_grid_view.setOnScrollListener(scrollListener)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        Log.i(TAG, "onSaveInstanceState")
        outState?.putParcelableArrayList(SAVE_LIST_STATE, truckItemList)
        super.onSaveInstanceState(outState)
    }

    override fun onSuccess(result: CollectionJson) {
        Log.i(TAG, "onSuccess")
        var next = result.collection.links.find { it.rel.equals("next") }?.href
        if (next == null)
            next = ""
        scrollListener.setNewLink(next)
        truckItemList.addAll(result.toTrucks())
        (truck_grid_view.adapter as ArrayAdapterTruck).notifyDataSetChanged()
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<CollectionJson>> {
        Log.i(TAG, "onCreateLoader")
        return AsyncTaskLoaderApi<CollectionJson>(CollectionJson::class.java,args.getParcelable(REQUEST),this@AllTrucksActivity)
    }

    override fun onLoaderReset(loader: Loader<Result<CollectionJson>>?) {
        Log.i(TAG, "onLoaderReset")
        loader?.stopLoading()
    }
}
