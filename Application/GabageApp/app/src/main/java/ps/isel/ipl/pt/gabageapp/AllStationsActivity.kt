package ps.isel.ipl.pt.gabageapp

import android.app.LoaderManager.LoaderCallbacks
import android.content.Context
import android.content.Loader
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_stations.*
import kotlinx.android.synthetic.main.content_stations.*
import ps.isel.ipl.pt.gabageapp.model.Station
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi.Companion.HOST_NAME_API
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterStation
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.scroll.EndlessScrollListener

class AllStationsActivity : AppCompatActivity(), LoaderCallbacksError<CollectionJson> {

    companion object {
        val GET_STATIONS = "getStations"
    }

    private val TAG = "StationsListActivity"
    private val LOADER_ID = 9687
    private val REQUEST = "request"
    private val SAVE_LIST_STATE = "stations_list_state"
    private lateinit var scrollListener: EndlessScrollListener
    private var stationItemList: ArrayList<Station> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stations)
        setSupportActionBar(toolbar)
        val url = intent.extras.get(GET_STATIONS)
        Log.i(TAG, "onCreate")

        scrollListener = object : EndlessScrollListener(20) {
            override fun onLoadMore(link: String): Boolean {
                Log.i(TAG, "New Link is load")
                var bundel = Bundle()
                var http = HttpLoader("${HOST_NAME_API}$link", arrayOf(Header("Accept","application/vnd.collection+json")))
                bundel.putParcelable(REQUEST,http)
                loaderManager.restartLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@AllStationsActivity)
                return true
            }
        }

        var saved = savedInstanceState?.getParcelableArrayList<Station>(SAVE_LIST_STATE)
        if(saved != null) {
            Log.i(TAG, "Reload list saved")
            stationItemList.addAll(0,saved)
        }else{
            Log.i(TAG, "Load frist time")
            var bundel = Bundle()
            var http = HttpLoader("${HOST_NAME_API}$url", arrayOf(Header("Accept", "application/vnd.collection+json")))
            bundel.putParcelable(REQUEST, http)
            loaderManager.initLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@AllStationsActivity)
        }

        stations_list_view.adapter = ArrayAdapterStation(this@AllStationsActivity, R.layout.station_item, stationItemList)
        (stations_list_view.adapter as ArrayAdapterStation).notifyDataSetChanged()
        stations_list_view.setOnScrollListener(scrollListener)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        Log.i(TAG, "onSaveInstanceState")
        outState?.putParcelableArrayList(SAVE_LIST_STATE, stationItemList)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<CollectionJson>>? {
        Log.i(TAG, "onCreateLoader")
        return AsyncTaskLoaderApi<CollectionJson>(CollectionJson::class.java,args.getParcelable(REQUEST),this@AllStationsActivity)
    }

    override fun onSuccess(result: CollectionJson) {
        Log.i(TAG, "onSuccess")
        var next = result.collection.links.find { it.rel.equals("next") }?.href
        if (next == null)
            next = ""
        scrollListener.setNewLink(next)
        stationItemList.addAll(result.toStations())
        (stations_list_view.adapter as ArrayAdapterStation).notifyDataSetChanged()
    }

    override fun onLoaderReset(loader: Loader<Result<CollectionJson>>?) {
        Log.i(TAG, "onLoaderReset")
        loader?.reset()
    }
}
