package ps.isel.ipl.pt.gabageapp.util.loader

import android.content.Context
import android.content.AsyncTaskLoader
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.google.gson.Gson
import ps.isel.ipl.pt.gabageapp.service_web_api.service.HttpRequestService.Companion.okHttpClient

/**
 * Created by goncalo on 24/05/2018.
 */

class AsyncTaskLoaderApi<T>(private val kclass: Class<T>, private var http: HttpLoader, context: Context) : AsyncTaskLoader<Result<T>>(context) {
    private var data: Result<T>? = null
    private val TAG = "AsyncTaskLoaderApi"

    override fun loadInBackground(): Result<T>? {
        Log.i(TAG, "Load " + http.url)
        val request = AndroidNetworking.get(http.url).setOkHttpClient(okHttpClient)

        http.headers.forEach {
            request.addHeaders(it.name, it.value)
        }
        val builded = request.build().executeForObject(kclass)
        if (builded.isSuccess){
            var result: T = builded.result as  T
            return Result(result, null)
        }else{
            var error = builded.error
            lateinit var errorObj: ErrorApi
            
            if(error.errorCode==0)
                errorObj = ErrorApi("Connection", "Connection Lost", error.errorCode, "Please, connect to a network", error.errorDetail)
            else if(error.errorCode >= 500)
                errorObj = ErrorApi("Bad Gateway", "Server Down", error.errorCode, "Sorry, our server is down", "Server is not responding")
            else
                errorObj = builded.error.getErrorAsObject(ErrorApi::class.java)

            Log.i(TAG, "Erro:  ${errorObj.detail}")
            return Result<T>(null, errorObj)
        }
    }

    override fun onStartLoading() {
        Log.i(TAG, "onStartLoading")
        super.onStartLoading()
        if(this.data != null) {
            deliverResult(data)
        }
        else
            forceLoad()
    }

    override fun forceLoad() {
        Log.i(TAG, "forceLoad")
        super.forceLoad()
    }

    override fun deliverResult(data: Result<T>?) {
        Log.i(TAG, "deliverResult")
        super.deliverResult(data)
        if(data != null && data.result !=null)
            this.data = data
    }

    override fun onReset() {
        Log.i(TAG, "onReset")
        super.onReset()
        if(data != null)
            data = null
    }

    override fun onStopLoading() {
        Log.i(TAG, "onStopLoading")
        super.onStopLoading()
        cancelLoad()
    }
}
