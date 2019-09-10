package ps.isel.ipl.pt.gabageapp.service_web_api.service

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import ps.isel.ipl.pt.gabageapp.util.loader.ErrorApi

/**
 * Created by goncalo on 15/06/2018.
 */
class ResultFromService(handler: Handler) : ResultReceiver(handler) {
    companion object {
        val RESULT_SUCCESS_CODE = 1100
        val RESULT_ERROR_CODE = 999
        val PARAM_EXCEPTION = "Erro"
        val PARAM_RESULT = "Success"
    }

    private lateinit var mReceiver: ResultFromService

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        super.onReceiveResult(resultCode, resultData)
        if(resultData != null && mReceiver != null) {
            when (resultCode) {
                RESULT_SUCCESS_CODE -> {
                    mReceiver.onSuccess(resultData.getString(PARAM_RESULT))
                }
                RESULT_ERROR_CODE -> mReceiver.onError(resultData.getParcelable(PARAM_EXCEPTION) as ErrorApi)
                else -> mReceiver.onError(resultData.getParcelable(PARAM_EXCEPTION) as ErrorApi)
            }
        }
    }

    fun setReceiver(callback: ResultFromService){
        mReceiver = callback
    }

    interface ResultFromService{
        fun onError(erro: ErrorApi)
        fun onSuccess(redirect: String?)
    }
}