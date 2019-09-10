package ps.isel.ipl.pt.gabageapp

import android.content.Loader
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_all_configurations.*
import kotlinx.android.synthetic.main.content_all_communications.*
import ps.isel.ipl.pt.gabageapp.model.ConfigurationItem
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi.Companion.HOST_NAME_API
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterConfigurations
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.scroll.EndlessScrollListener

class AllConfigurations : AppCompatActivity(), LoaderCallbacksError<CollectionJson> {

    companion object {
        val GET_CONFIGURATIONS= "getCommunications"
    }

    private val LOADER_ID = 1177
    private val REQUEST = "request"
    private val SAVE_LIST_STATE = "communications_list_state"
    private lateinit var scrollListener: EndlessScrollListener
    private var configurationItemList: ArrayList<ConfigurationItem> = arrayListOf()
    private val TAG = "AllConfigurations"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_configurations)
        setSupportActionBar(toolbar)

        var url = intent.extras.get(GET_CONFIGURATIONS)
        Log.i(TAG, "onCreate")

        scrollListener = object : EndlessScrollListener(20) {
            override fun onLoadMore(link: String): Boolean {
                Log.i(TAG, "New Link is load")
                var bundel = Bundle()
                var http = HttpLoader("$HOST_NAME_API$link", arrayOf(Header("Accept","application/vnd.collection+json")))
                bundel.putParcelable(REQUEST,http)
                loaderManager.restartLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@AllConfigurations)
                return true
            }
        }

        var saved = savedInstanceState?.getParcelableArrayList<ConfigurationItem>(SAVE_LIST_STATE)
        if(saved != null) {
            Log.i(TAG, "Reload list saved")
            configurationItemList.addAll(0,saved)
        }else{
            Log.i(TAG, "Load frist time")
            var bundel = Bundle()
            var http = HttpLoader("$HOST_NAME_API$url?showInactive=true", arrayOf(Header("Accept", "application/vnd.collection+json")))
            bundel.putParcelable(REQUEST, http)
            loaderManager.initLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@AllConfigurations)
        }

        configurations_list.adapter = ArrayAdapterConfigurations(this@AllConfigurations, R.layout.configuration_item, configurationItemList)
        (configurations_list.adapter as ArrayAdapterConfigurations).notifyDataSetChanged()
        configurations_list.setOnScrollListener(scrollListener)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        Log.i(TAG, "onSaveInstanceState")
        outState?.putParcelableArrayList(SAVE_LIST_STATE, configurationItemList)
        super.onSaveInstanceState(outState)
    }
    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<CollectionJson>>? {
        Log.i(TAG, "onCreateLoader")
        return AsyncTaskLoaderApi<CollectionJson>(CollectionJson::class.java,args.getParcelable(REQUEST),this@AllConfigurations)
    }

    override fun onSuccess(result: CollectionJson) {
        Log.i(TAG, "onSuccess")
        var next = result.collection.links.find { it.rel.equals("next") }?.href
        if (next == null)
            next = ""
        scrollListener.setNewLink(next)
        configurationItemList.addAll(result.toConfigurations())
        (configurations_list.adapter as ArrayAdapterConfigurations).notifyDataSetChanged()
    }

    override fun onLoaderReset(loader: Loader<Result<CollectionJson>>?) {
        Log.i(TAG, "onLoaderReset")
        loader?.reset()
    }
}
