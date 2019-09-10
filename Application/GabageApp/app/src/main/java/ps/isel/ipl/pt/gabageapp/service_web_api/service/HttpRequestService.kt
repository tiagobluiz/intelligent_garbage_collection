package ps.isel.ipl.pt.gabageapp.service_web_api.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.ANRequest
import com.google.gson.Gson
import okhttp3.*
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi.Companion.HOST_NAME_API
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Action
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Field
import org.json.JSONObject
import ps.isel.ipl.pt.gabageapp.util.loader.ErrorApi
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import ps.isel.ipl.pt.gabageapp.service_web_api.service.ResultFromService.Companion.PARAM_EXCEPTION
import ps.isel.ipl.pt.gabageapp.service_web_api.service.ResultFromService.Companion.PARAM_RESULT
import ps.isel.ipl.pt.gabageapp.service_web_api.service.ResultFromService.Companion.RESULT_ERROR_CODE
import ps.isel.ipl.pt.gabageapp.service_web_api.service.ResultFromService.Companion.RESULT_SUCCESS_CODE


/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class HttpRequestService : IntentService("HttpRequestService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            val http = intent.extras.getParcelable<Action>(EXTRA_PARAM1)
            val rceiverResult = intent.extras.getParcelable<ResultReceiver>(EXTRA_PARAM2)
            if (ACTION_POST.equals(action)) {
                handleActionPost(http, rceiverResult)
            } else if (ACTION_PUT.equals(action)) {
                handleActionPut(http, rceiverResult)
            } else if (ACTION_DELETE.equals(action)){
                handleActionDelete(http, rceiverResult)
            }
        }
    }

    private fun handleAction(action: Action,
                             request: ANRequest.PostRequestBuilder<out ANRequest.PostRequestBuilder<*>>,
                             resulReceiver: ResultReceiver){
        //request.addHeaders("Authorization", AUTHVALUE)

        Log.i("HttpService.result", "body")

        if(!action.fields.isEmpty()) {
            request.addHeaders("Content-Type",action.type)
            request.addJSONObjectBody(parseBody(action.fields))
        }
        val bundle = Bundle()
        var code = RESULT_ERROR_CODE

        val builded = request.build().executeForOkHttpResponse()
        if(builded.isSuccess) {
            val response = builded.okHttpResponse
            if(response.isSuccessful){
                code = RESULT_SUCCESS_CODE
                bundle.putString(PARAM_RESULT, response.header("Location"))
                Log.i("HttpService.result", "done")
            }
            else if(response.code()>=500)
                bundle.putParcelable(PARAM_EXCEPTION, ErrorApi("Bad Gateway", "Server Down", response.code(), "Sorry, our server is down", "Server is not responding"))
            else if(response.code() >=400 && response.code()<500){
                if(response.body() != null){
                    var errorObj = gson.fromJson<ErrorApi>(response.body()?.charStream()?.readText(), ErrorApi::class.java)
                    bundle.putParcelable(PARAM_EXCEPTION, errorObj)
                    Log.i("HttpService.result", "ErroAPI:  ${errorObj.detail}")
                }
            }
        }
        else {
            var error = builded.error
            lateinit var errorObj: ErrorApi

            if(error.errorCode==0)
                errorObj = ErrorApi("Connection", "Connection Lost", error.errorCode, "Please, connect to a network", error.errorDetail)

            Log.i("HttpService.result", "Erro:  ${errorObj.detail}")
            bundle.putParcelable(PARAM_EXCEPTION, errorObj)
        }
        resulReceiver.send(code, bundle)
    }

    private fun parseBody( fields :ArrayList<Field>): JSONObject{
        val ret = JSONObject()
        fields.forEach {

            ret.put(it.name, it.value)
        }
        return ret
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionPost(action: Action, resulReceiver: ResultReceiver){
        val request = AndroidNetworking.post("${HOST_NAME_API}${action.href}").setOkHttpClient(okHttpClient)
        handleAction(action, request, resulReceiver)
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionPut(action: Action, resulReceiver: ResultReceiver) {
        val request = AndroidNetworking.put("${HOST_NAME_API}${action.href}").setOkHttpClient(okHttpClient)
        handleAction(action, request, resulReceiver)
    }

    private fun handleActionDelete(action: Action, resulReceiver: ResultReceiver){
        val request = AndroidNetworking.delete("${HOST_NAME_API}${action.href}").setOkHttpClient(okHttpClient)
        handleAction(action, request, resulReceiver)
    }

    companion object: RequestService {
        // TODO: Rename actions, choose action names that describe tasks that this
        // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
        private val ACTION_POST = "ps.isel.ipl.pt.gabageapp.util.siren_request.action.POST"
        private val ACTION_PUT = "ps.isel.ipl.pt.gabageapp.util.siren_request.action.PUT"
        private val ACTION_DELETE = "ps.isel.ipl.pt.gabageapp.util.siren_request.action.DELETE"
        private val ACTION = "ps.isel.ipl.pt.gabageapp.util.siren_request.action."
        // TODO: Rename parameters
        private val EXTRA_PARAM1 = "ps.isel.ipl.pt.gabageapp.util.siren_request.extra.ACTION"
        private val EXTRA_PARAM2 = "ps.isel.ipl.pt.gabageapp.util.siren_request.extra.RECEIVE_RESULT"
        private val gson = Gson()
        var okHttpClient : OkHttpClient = OkHttpClient.Builder().authenticator(object : Authenticator {
            override fun authenticate(route: Route, response: Response): Request {
                return response.request().newBuilder().build()
            }
        }).build()

        fun setAuth(authHeader : String){
            okHttpClient = OkHttpClient.Builder().authenticator(object : Authenticator {
                override fun authenticate(route: Route, response: Response): Request {
                    return response.request().newBuilder()
                            .header("Authorization", "Basic $authHeader")
                            .build()
                }
            }).build()
        }

        override fun startAction(context: Context, param1: Action, param2: ResultFromService.ResultFromService) {
            val serviceResultReceiver = ResultFromService(Handler(context.mainLooper))
            serviceResultReceiver.setReceiver(param2)

            val intent = Intent(context, HttpRequestService::class.java)
            intent.action = "${ACTION}${param1.method}"
            intent.putExtra(EXTRA_PARAM1, param1)
            intent.putExtra(EXTRA_PARAM2, serviceResultReceiver)
            context.startService(intent)
        }
    }
}
