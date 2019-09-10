package ps.isel.ipl.pt.gabageapp.service_web_api.service

import android.app.AlertDialog
import android.content.Context
import ps.isel.ipl.pt.gabageapp.util.loader.ErrorApi

/**
 * Created by goncalo on 15/06/2018.
 */
abstract class ResulFromServiceErro(val context: Context) : ResultFromService.ResultFromService {
    override fun onError(erro: ErrorApi) {
        val dlgAlert = AlertDialog.Builder(context)
                .setMessage(erro.message)
                .setTitle("Erro")
                .setPositiveButton("OK") { dialog, which ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .create()
        dlgAlert.show()
    }
}