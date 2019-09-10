package ps.isel.ipl.pt.gabageapp.service_web_api.service

import android.content.Context
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Action

/**
 * Created by goncalo on 10/07/2018.
 */
interface RequestService {
    fun startAction(context: Context, param1: Action, param2: ResultFromService.ResultFromService)
}