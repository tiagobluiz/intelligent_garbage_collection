package ps.isel.ipl.pt.gabageapp

import android.app.LoaderManager.LoaderCallbacks
import android.content.Loader
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

import kotlinx.android.synthetic.main.activity_containers.*
import kotlinx.android.synthetic.main.content_containers.*
import ps.isel.ipl.pt.gabageapp.model.ContainerItem
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi.Companion.HOST_NAME_API
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.scroll.EndlessScrollListener

abstract class ContainersActivity : AppCompatActivity(), LoaderCallbacks<Result<CollectionJson>> {

    companion object {
        val GET_CONTAINERS= "getRouteContainers"
    }

    private val LOADER_ID = 1597
    private val REQUEST = "request"
    private val SAVE_LIST_STATE = "container_list_state"
    private lateinit var scrollListener: EndlessScrollListener
    private var containerItemList: ArrayList<ContainerItem> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_containers)
        setSupportActionBar(toolbar)

        var url = intent.extras.get(GET_CONTAINERS)

        scrollListener = object : EndlessScrollListener(20) {
            override fun onLoadMore(link: String): Boolean {
                var bundel = Bundle()
                var http = HttpLoader("${HOST_NAME_API}$link", arrayOf(Header("Accept","application/vnd.collection+json")))
                bundel.putParcelable(REQUEST,http)
                loaderManager.restartLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@ContainersActivity)
                return true
            }
        }
        var saved = savedInstanceState?.getParcelableArrayList<ContainerItem>(SAVE_LIST_STATE)
        if(saved != null) {
            Log.i("Collect_Zone_List", "CreateSaved")
            containerItemList.addAll(0,saved)
        }else{

            var bundel = Bundle()
            var http = HttpLoader("$HOST_NAME_API$url?showInactive=true", arrayOf(Header("Accept", "application/vnd.collection+json")))
            bundel.putParcelable(REQUEST, http)
            loaderManager.initLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@ContainersActivity)
        }

        inititialize(containerItemList)
        containers_list_view.setOnScrollListener(scrollListener)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putParcelableArrayList(SAVE_LIST_STATE, containerItemList)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<CollectionJson>>? {
        return AsyncTaskLoaderApi<CollectionJson>(CollectionJson::class.java,args.getParcelable(REQUEST),this@ContainersActivity)
    }

    override fun onLoadFinished(loader: Loader<Result<CollectionJson>>?, data: Result<CollectionJson>) {
        if(data.result!=null) {
            var next = data.result.collection.links.find { it.rel.equals("next") }?.href
            if (next == null)
                next = ""
            scrollListener.setNewLink(next)
            containerItemList.addAll(data.result.toContainers())
            notifyChange()
        }
        else if(data.error!=null){

        }
    }

    override fun onLoaderReset(loader: Loader<Result<CollectionJson>>?) {
        loader?.reset()
    }

    open fun addNewItem(newContainer: ContainerItem){
        containerItemList.add(newContainer)
        notifyChange()
    }

    abstract fun inititialize(list: ArrayList<ContainerItem>)

    abstract fun notifyChange()

}
