package ps.isel.ipl.pt.gabageapp.util.loader

import android.app.AlertDialog
import android.app.LoaderManager
import android.content.Loader
import android.support.v7.app.AppCompatActivity

/**
 * Created by goncalo on 13/06/2018.
 */
interface LoaderCallbacksError<T> : LoaderManager.LoaderCallbacks<Result<T>> {

    override fun onLoadFinished(loader: Loader<Result<T>>?, data: Result<T>) {
        if(loader!=null) {
            if (data.result != null) {
                onSuccess(data.result)
            } else if (data.error != null) {
                if(loader!=null) {
                    val dlgAlert = AlertDialog.Builder(this as AppCompatActivity)
                            .setMessage(data.error.message)
                            .setTitle("Erro")
                            .setPositiveButton("OK") { dialog, which ->
                                dialog.dismiss()
                                loader.startLoading()
                            }
                            .setCancelable(false)
                            .create()
                    dlgAlert.show()
                }
            }
        }
    }

    fun onSuccess(result: T)
}