package ps.isel.ipl.pt.gabageapp

import android.content.Loader
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_container_collects.*
import kotlinx.android.synthetic.main.content_container_collects.*
import ps.isel.ipl.pt.gabageapp.model.ContainerCollect
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterContainerCollects
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.scroll.EndlessScrollListener

class ContainerCollects : AppCompatActivity() , LoaderCallbacksError<CollectionJson> {

    companion object {
        val GET_CONTAINER_COLLECTS = "getContainerCollects"
    }
    private val TAG = "ContaiColleListActivity"
    private val LOADER_ID = 5873
    private val REQUEST = "request"
    private val SAVE_LIST_STATE = "collects_list_state"
    private lateinit var scrollListener: EndlessScrollListener
    private var collectItemList: ArrayList<ContainerCollect> = arrayListOf()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_collects)
        setSupportActionBar(toolbar)
        Log.i(TAG, "onCreate")
        var url = intent.extras.get(GET_CONTAINER_COLLECTS)

        scrollListener = object : EndlessScrollListener(20) {
            override fun onLoadMore(link: String): Boolean {
                Log.i(TAG, "New Link is load")
                var bundel = Bundle()
                var http = HttpLoader("${WasteManageApi.HOST_NAME_API}$link", arrayOf(Header("Accept","application/vnd.collection+json")))
                bundel.putParcelable(REQUEST,http)
                loaderManager.restartLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@ContainerCollects)
                return true
            }
        }

        var saved = savedInstanceState?.getParcelableArrayList<ContainerCollect>(SAVE_LIST_STATE)
        if(saved != null) {
            Log.i(TAG, "Reload list saved")
            collectItemList.addAll(0,saved)
        }else{
            Log.i(TAG, "Load frist time")
            var bundel = Bundle()
            var http = HttpLoader("${WasteManageApi.HOST_NAME_API}$url", arrayOf(Header("Accept", "application/vnd.collection+json")))
            bundel.putParcelable(REQUEST, http)
            loaderManager.initLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@ContainerCollects)
        }
        container_collects_list.adapter = ArrayAdapterContainerCollects(this@ContainerCollects, R.layout.wash_item, collectItemList)
        (container_collects_list.adapter as ArrayAdapterContainerCollects).notifyDataSetChanged()
        container_collects_list.setOnScrollListener(scrollListener)

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        Log.i(TAG, "onSaveInstanceState")
        outState?.putParcelableArrayList(SAVE_LIST_STATE, collectItemList)
        super.onSaveInstanceState(outState)
    }

    override fun onSuccess(result: CollectionJson) {
        Log.i(TAG, "onSuccess")
        var next = result.collection.links.find { it.rel.equals("next") }?.href
        if (next == null)
            next = ""
        scrollListener.setNewLink(next)
        collectItemList.addAll(result.toContainerCollects())
        (container_collects_list.adapter as ArrayAdapterContainerCollects).notifyDataSetChanged()
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<CollectionJson>> {
        Log.i(TAG, "onCreateLoader")
        return AsyncTaskLoaderApi<CollectionJson>(CollectionJson::class.java,args.getParcelable(REQUEST),this@ContainerCollects)
    }

    override fun onLoaderReset(loader: Loader<Result<CollectionJson>>?) {
        Log.i(TAG, "onLoaderReset")
        loader?.stopLoading()
    }

}
