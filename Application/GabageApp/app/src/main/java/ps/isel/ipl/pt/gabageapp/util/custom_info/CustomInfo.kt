package ps.isel.ipl.pt.gabageapp.util.custom_info

import android.view.MotionEvent
import android.view.View

/**
 * Created by goncalo on 26/06/2018.
 */
class CustomInfo(val collectZoneId: Int) {
    lateinit var collectListener : ((v: View)->Unit)
    lateinit var washListener : ((v: View)->Unit)
}