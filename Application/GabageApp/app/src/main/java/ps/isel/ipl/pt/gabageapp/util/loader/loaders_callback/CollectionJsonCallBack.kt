package ps.isel.ipl.pt.gabageapp.util.loader.loaders_callback

import android.app.LoaderManager.LoaderCallbacks
import android.content.Context
import android.content.Loader
import android.os.Bundle
import ps.isel.ipl.pt.gabageapp.util.loader.AsyncTaskLoaderApi
import ps.isel.ipl.pt.gabageapp.util.loader.LoaderCallbacksError
import ps.isel.ipl.pt.gabageapp.util.loader.Result
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson

/**
 * Created by goncalo on 29/05/2018.
 */
abstract class CollectionJsonCallBack(val context: Context, val keyWord: String) : LoaderCallbacksError<CollectionJson> {

    override fun onCreateLoader(p0: Int, args: Bundle): Loader<Result<CollectionJson>> {
        return AsyncTaskLoaderApi<CollectionJson>(CollectionJson::class.java, args.getParcelable(keyWord), context )
    }

    override fun onLoaderReset(loader: Loader<Result<CollectionJson>>?) {
        loader?.stopLoading()
    }
}