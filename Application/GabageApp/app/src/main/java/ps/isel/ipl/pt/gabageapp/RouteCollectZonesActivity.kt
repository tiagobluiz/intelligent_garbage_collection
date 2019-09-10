package ps.isel.ipl.pt.gabageapp

import android.content.Loader
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_route_collect_zones.*
import kotlinx.android.synthetic.main.content_route_collect_zones.*
import ps.isel.ipl.pt.gabageapp.model.CollectZoneItem
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi.Companion.HOST_NAME_API
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterCollectZone
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.scroll.EndlessScrollListener

class RouteCollectZonesActivity : AppCompatActivity(), LoaderCallbacksError<CollectionJson>  {

    companion object {
        val GET_COLLECTZONES = "getCollectZones"
    }

    private val TAG = "CollectZoneListActivity"
    private val LOADER_ID = 3698
    private val REQUEST = "request"
    private var collectZoneList: ArrayList<CollectZoneItem> = arrayListOf()
    private lateinit var scrollListener: EndlessScrollListener
    private val SAVE_LIST_STATE = "route_list_state"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_collect_zones)
        setSupportActionBar(toolbar)
        var url: String = intent.extras.getString(GET_COLLECTZONES)
        Log.i(TAG, "onCreate")

        scrollListener = object : EndlessScrollListener(7) {
            override fun onLoadMore(link: String): Boolean {
                Log.i(TAG, "New Link is load")
                var bundel = Bundle()
                var http = HttpLoader("$HOST_NAME_API$link", arrayOf(Header("Accept","application/vnd.collection+json")))
                bundel.putParcelable(REQUEST,http)
                loaderManager.restartLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@RouteCollectZonesActivity)
                return true
            }
        }

        var saved = savedInstanceState?.getParcelableArrayList<CollectZoneItem>(SAVE_LIST_STATE)
        if(saved != null) {
            Log.i(TAG, "Reload list saved")
            collectZoneList.addAll(0,saved)
        }else{
            Log.i(TAG, "Load frist time")
            var bundel = Bundle()
            var http = HttpLoader("$HOST_NAME_API$url?showInactive=true", arrayOf(Header("Accept","application/vnd.collection+json")))
            bundel.putParcelable(REQUEST,http)
            loaderManager.initLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@RouteCollectZonesActivity)
        }

        collect_zones_list.adapter = ArrayAdapterCollectZone(this@RouteCollectZonesActivity, R.layout.collect_zone_item, collectZoneList)
        (collect_zones_list.adapter as ArrayAdapterCollectZone).notifyDataSetChanged()
        collect_zones_list.setOnScrollListener(scrollListener)

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        Log.i(TAG, "onSaveInstanceState")
        outState?.putParcelableArrayList(SAVE_LIST_STATE, collectZoneList)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<CollectionJson>> {
        Log.i(TAG, "onCreateLoader")
        return AsyncTaskLoaderApi<CollectionJson>(CollectionJson::class.java,args.getParcelable(REQUEST),this@RouteCollectZonesActivity)
    }

    override fun onSuccess(result: CollectionJson) {
        Log.i(TAG, "onSuccess")
        var next = result.collection.links.find { it.rel.equals("next") }?.href
        if (next == null)
            next = ""
        scrollListener.setNewLink(next)
        collectZoneList.addAll(result.toCollectZones())
        (collect_zones_list.adapter as ArrayAdapterCollectZone).notifyDataSetChanged()
    }

    override fun onLoaderReset(loader: Loader<Result<CollectionJson>>?) {
        Log.i(TAG, "onLoaderReset")
        loader?.stopLoading()
    }
}
