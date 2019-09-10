package ps.isel.ipl.pt.gabageapp

import android.content.Loader
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_washes.*
import kotlinx.android.synthetic.main.content_washes.*
import ps.isel.ipl.pt.gabageapp.model.Wash
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterWashes
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.scroll.EndlessScrollListener

class WashesActivity : AppCompatActivity(), LoaderCallbacksError<CollectionJson> {

    companion object {
        val GET_WASHES = "getWashes"
    }

    private val TAG = "WashesListActivity"
    private val LOADER_ID = 6932
    private val REQUEST = "request"
    private val SAVE_LIST_STATE = "wash_list_state"
    private lateinit var scrollListener: EndlessScrollListener
    private var washesItemList: ArrayList<Wash> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_washes)
        setSupportActionBar(toolbar)

        var url = intent.extras.get(GET_WASHES)
        Log.i(TAG, "onCreate")
        scrollListener = object : EndlessScrollListener(20) {
            override fun onLoadMore(link: String): Boolean {
                Log.i(TAG, "New Link is load")
                var bundel = Bundle()
                var http = HttpLoader("${WasteManageApi.HOST_NAME_API}$link", arrayOf(Header("Accept","application/vnd.collection+json")))
                bundel.putParcelable(REQUEST,http)
                loaderManager.restartLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@WashesActivity)
                return true
            }
        }

        var saved = savedInstanceState?.getParcelableArrayList<Wash>(SAVE_LIST_STATE)
        if(saved != null) {
            Log.i(TAG, "Reload list saved")
            washesItemList.addAll(0,saved)
        }else{
            Log.i(TAG, "Load frist time")
            var bundel = Bundle()
            var http = HttpLoader("${WasteManageApi.HOST_NAME_API}$url", arrayOf(Header("Accept", "application/vnd.collection+json")))
            bundel.putParcelable(REQUEST, http)
            loaderManager.initLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@WashesActivity)
        }

        washes_grid.adapter = ArrayAdapterWashes(this@WashesActivity, R.layout.wash_item, washesItemList)
        (washes_grid.adapter as ArrayAdapterWashes).notifyDataSetChanged()
        washes_grid.setOnScrollListener(scrollListener)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        Log.i(TAG, "onSaveInstanceState")
        outState?.putParcelableArrayList(SAVE_LIST_STATE, washesItemList)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<CollectionJson>> {
        Log.i(TAG, "onCreateLoader")
        return AsyncTaskLoaderApi<CollectionJson>(CollectionJson::class.java,args.getParcelable(REQUEST),this@WashesActivity)
    }

    override fun onSuccess(result: CollectionJson) {
        Log.i(TAG, "onSuccess")
        var next = result.collection.links.find { it.rel.equals("next") }?.href
        if (next == null)
            next = ""
        scrollListener.setNewLink(next)
        washesItemList.addAll(result.toWashes())
        (washes_grid.adapter as ArrayAdapterWashes).notifyDataSetChanged()
    }

    override fun onLoaderReset(loader: Loader<Result<CollectionJson>>?) {
        Log.i(TAG, "onLoaderReset")
        loader?.reset()
    }
}
