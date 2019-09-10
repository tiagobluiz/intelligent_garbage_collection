package ps.isel.ipl.pt.gabageapp.model

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import ps.isel.ipl.pt.gabageapp.MapEmployeeWork
import ps.isel.ipl.pt.gabageapp.manage.ManageSpaceWork
import ps.isel.ipl.pt.gabageapp.manage.ManageSpaceWork.Companion.GET_HOME
import ps.isel.ipl.pt.gabageapp.MapEmployeeWork.Companion.GET_HOME_MAP
import ps.isel.ipl.pt.gabageapp.util.loader.json_home.HomeJson

/**
 * Created by goncalo on 09/04/2018.
 */
class User(val username: String, val post: String, val email: String, val phoneNumber: Int){
    private val ADMINROLE = "administrator"
    private val COLLECTER = "collector"
    private val MANAGEMENT = "management"

    fun action(context: Context, home : HomeJson){
        var intent : Intent? = null
        if(ADMINROLE.equals(post) || MANAGEMENT.equals(post)){
            intent = Intent(context, ManageSpaceWork::class. java)
            intent.putExtra(GET_HOME, home)
        }else if(COLLECTER.equals(post)){
            intent = Intent(context, MapEmployeeWork::class. java)
            intent.putExtra(GET_HOME_MAP, home)
        }
        if(intent != null) {
            ContextCompat.startActivity(context, intent, null)
        }
    }
}
