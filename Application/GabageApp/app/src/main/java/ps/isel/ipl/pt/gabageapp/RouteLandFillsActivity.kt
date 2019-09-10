package ps.isel.ipl.pt.gabageapp

import android.content.Loader
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_route_land_fills.*
import kotlinx.android.synthetic.main.content_route_land_fills.*
import ps.isel.ipl.pt.gabageapp.model.Landfill
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterLandFill
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.scroll.EndlessScrollListener

class RouteLandFillsActivity : AppCompatActivity(), LoaderCallbacksError<CollectionJson> {

    companion object {
        val GET_LANDFILL="getRouteLandFill"
    }

    private val TAG = "LandFillListActivity"
    private val LOADER_ID = 1384
    private val REQUEST = "request"
    private val SAVE_LIST_STATE = "truck_list_state"
    private lateinit var scrollListener: EndlessScrollListener
    private var truckItemList: ArrayList<Landfill> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_land_fills)
        setSupportActionBar(toolbar)

        var url = intent.extras.get(GET_LANDFILL)
        Log.i(TAG, "onCreate")

        scrollListener = object : EndlessScrollListener(20) {
            override fun onLoadMore(link: String): Boolean {
                Log.i(TAG, "New Link is load")
                var bundel = Bundle()
                var http = HttpLoader("${WasteManageApi.HOST_NAME_API}$link", arrayOf(Header("Accept","application/vnd.collection+json")))
                bundel.putParcelable(REQUEST,http)
                loaderManager.restartLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@RouteLandFillsActivity)
                return true
            }
        }

        var saved = savedInstanceState?.getParcelableArrayList<Landfill>(SAVE_LIST_STATE)
        if(saved != null) {
            Log.i(TAG, "Reload list saved")
            truckItemList.addAll(0,saved)
        }else{
            Log.i(TAG, "Load frist time")
            var bundel = Bundle()
            var http = HttpLoader("${WasteManageApi.HOST_NAME_API}$url?showInactive=true", arrayOf(Header("Accept", "application/vnd.collection+json")))
            bundel.putParcelable(REQUEST, http)
            loaderManager.initLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@RouteLandFillsActivity)
        }

        landfil_list_view.adapter = ArrayAdapterLandFill(this@RouteLandFillsActivity, R.layout.landfill_item, truckItemList)
        (landfil_list_view.adapter as ArrayAdapterLandFill).notifyDataSetChanged()
        landfil_list_view.setOnScrollListener(scrollListener)
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
        truckItemList.addAll(result.toLandFills())
        (landfil_list_view.adapter as ArrayAdapterLandFill).notifyDataSetChanged()
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<CollectionJson>> {
        Log.i(TAG, "onCreateLoader")
        return AsyncTaskLoaderApi<CollectionJson>(CollectionJson::class.java,args.getParcelable(REQUEST),this@RouteLandFillsActivity)
    }

    override fun onLoaderReset(loader: Loader<Result<CollectionJson>>?) {
        Log.i(TAG, "onLoaderReset")
        loader?.stopLoading()
    }
}
