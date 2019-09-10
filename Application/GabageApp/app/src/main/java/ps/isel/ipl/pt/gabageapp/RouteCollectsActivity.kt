package ps.isel.ipl.pt.gabageapp

import android.app.LoaderManager.LoaderCallbacks
import android.content.Loader
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_collects.*
import kotlinx.android.synthetic.main.content_collects.*
import ps.isel.ipl.pt.gabageapp.model.RouteCollectItem
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterRouteCollects
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.scroll.EndlessScrollListener

class RouteCollectsActivity : AppCompatActivity(), LoaderCallbacks<Result<CollectionJson>> {

    companion object {
        val GET_COLLECTS = "getCollects"
    }

    private val LOADER_ID = 1548
    private val REQUEST = "request"
    private val SAVE_LIST_STATE = "collects_list_state"
    private lateinit var scrollListener: EndlessScrollListener
    private var collectItemList: ArrayList<RouteCollectItem> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collects)
        setSupportActionBar(toolbar)
        var url = intent.extras.get(GET_COLLECTS)

        scrollListener = object : EndlessScrollListener(20) {
            override fun onLoadMore(link: String): Boolean {
                var bundel = Bundle()
                var http = HttpLoader("${WasteManageApi.HOST_NAME_API}$link", arrayOf(Header("Accept","application/vnd.collection+json")))
                bundel.putParcelable(REQUEST,http)
                loaderManager.restartLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@RouteCollectsActivity)
                return true
            }
        }

        var saved = savedInstanceState?.getParcelableArrayList<RouteCollectItem>(SAVE_LIST_STATE)
        if(saved != null) {
            Log.i("Collect_Zone_List", "CreateSaved")
            collectItemList.addAll(0,saved)
        }else{

            var bundel = Bundle()
            var http = HttpLoader("${WasteManageApi.HOST_NAME_API}$url", arrayOf(Header("Accept", "application/vnd.collection+json")))
            bundel.putParcelable(REQUEST, http)
            loaderManager.initLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@RouteCollectsActivity)
        }
        collects_list.adapter = ArrayAdapterRouteCollects(this@RouteCollectsActivity, R.layout.collect_item, collectItemList)
        (collects_list.adapter as ArrayAdapterRouteCollects).notifyDataSetChanged()
        collects_list.setOnScrollListener(scrollListener)
    }

    override fun onCreateLoader(p0: Int, args: Bundle): Loader<Result<CollectionJson>> {
        return AsyncTaskLoaderApi<CollectionJson>(CollectionJson::class.java,args.getParcelable(REQUEST),this@RouteCollectsActivity)
    }

    override fun onLoadFinished(p0: Loader<Result<CollectionJson>>?, data: Result<CollectionJson>) {
        if(data.result!= null) {
            var next = data.result.collection.links.find { it.rel.equals("next") }?.href
            if (next == null)
                next = ""
            scrollListener.setNewLink(next)
            collectItemList.addAll(data.result.toRouteCollects())
            (collects_list.adapter as ArrayAdapterRouteCollects).notifyDataSetChanged()
        }
        else if(data.error!=null){

        }
    }

    override fun onLoaderReset(loader: Loader<Result<CollectionJson>>?) {
        loader?.stopLoading()
    }
}
