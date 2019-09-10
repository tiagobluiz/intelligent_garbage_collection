package ps.isel.ipl.pt.gabageapp.util.loader.v4

import android.content.Context
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import ps.isel.ipl.pt.gabageapp.util.loader.Result
import kotlin.coroutines.experimental.coroutineContext

/**
 * Created by goncalo on 26/06/2018.
 */
interface LoaderCallbacksErrorV4<T> : LoaderManager.LoaderCallbacks<Result<T>> {
    override fun onLoadFinished(loader: Loader<Result<T>>?, data: Result<T>) {
        if(loader!=null) {
            if (data.result != null) {
                onSuccess(data.result)
            } else if (data.error != null) {
                if(loader!=null) {
                    //val dlgAlert1 = DialogFragment()
                    val dlgAlert = AlertDialog.Builder(this as FragmentActivity)
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