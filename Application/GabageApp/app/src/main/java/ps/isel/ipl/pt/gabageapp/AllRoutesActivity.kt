package ps.isel.ipl.pt.gabageapp

import android.app.AlertDialog
import android.os.Bundle
import android.app.LoaderManager.LoaderCallbacks
import android.content.Context
import android.content.Loader
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_all_route.*
import ps.isel.ipl.pt.gabageapp.model.RouteItem
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi.Companion.HOST_NAME_API
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterRoute
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.scroll.EndlessScrollListener





class AllRoutesActivity : AppCompatActivity(), LoaderCallbacksError<CollectionJson> {

    companion object {
        val GET_ROUTES = "getRoutes"
    }

    private val TAG = "RoutesListActivity"
    private val LOADER_ID = 2589
    private val REQUEST = "request"
    private val SAVE_LIST_STATE = "route_list_state"
    private lateinit var scrollListener: EndlessScrollListener
    private var routeItemList: ArrayList<RouteItem> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_route)
        setSupportActionBar(toolbar)
        var url = intent.extras.get(GET_ROUTES)
        Log.i(TAG, "onCreate")

        scrollListener = object : EndlessScrollListener(20) {
            override fun onLoadMore(link: String): Boolean {
                Log.i(TAG, "New Link is load")
                var bundel = Bundle()
                var http = HttpLoader("$HOST_NAME_API$link", arrayOf(Header("Accept","application/vnd.collection+json")))
                bundel.putParcelable(REQUEST,http)
                loaderManager.restartLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@AllRoutesActivity)
                return true
            }
        }

        var saved = savedInstanceState?.getParcelableArrayList<RouteItem>(SAVE_LIST_STATE)
        if(saved != null) {
            Log.i(TAG, "Reload list saved")
            routeItemList.addAll(0,saved)
        }else{
            Log.i(TAG, "Load frist time")
            var bundel = Bundle()
            var http = HttpLoader("$HOST_NAME_API$url?showInactive=true", arrayOf(Header("Accept", "application/vnd.collection+json")))
            bundel.putParcelable(REQUEST, http)
            loaderManager.initLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@AllRoutesActivity)
        }

        routes_litsview.adapter = ArrayAdapterRoute(this@AllRoutesActivity, R.layout.route_item, routeItemList)
        (routes_litsview.adapter as ArrayAdapterRoute).notifyDataSetChanged()
        routes_litsview.setOnScrollListener(scrollListener)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        Log.i(TAG, "onSaveInstanceState")
        outState?.putParcelableArrayList(SAVE_LIST_STATE, routeItemList)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<CollectionJson>>? {
        Log.i(TAG, "onCreateLoader")
        return AsyncTaskLoaderApi<CollectionJson>(CollectionJson::class.java,args.getParcelable(REQUEST),this@AllRoutesActivity)
    }

    override fun onSuccess(result: CollectionJson) {
        Log.i(TAG, "onSuccess")
        var next = result.collection.links.find { it.rel.equals("next") }?.href
        if (next == null)
            next = ""
        scrollListener.setNewLink(next)
        routeItemList.addAll(result.toRoutes())
        (routes_litsview.adapter as ArrayAdapterRoute).notifyDataSetChanged()
    }

    override fun onLoaderReset(loader: Loader<Result<CollectionJson>>?) {
        Log.i(TAG, "onLoaderReset")
        loader?.reset()
    }
}
