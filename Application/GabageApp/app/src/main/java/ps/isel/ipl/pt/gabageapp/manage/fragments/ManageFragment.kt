package ps.isel.ipl.pt.gabageapp.manage.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_manage.*
import ps.isel.ipl.pt.gabageapp.*
import ps.isel.ipl.pt.gabageapp.AllConfigurations.Companion.GET_CONFIGURATIONS
import ps.isel.ipl.pt.gabageapp.AllRoutesActivity.Companion.GET_ROUTES
import ps.isel.ipl.pt.gabageapp.AllStationsActivity.Companion.GET_STATIONS
import ps.isel.ipl.pt.gabageapp.AllTrucksActivity.Companion.GET_TRUCKS
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi
import ps.isel.ipl.pt.gabageapp.util.loader.Header
import ps.isel.ipl.pt.gabageapp.util.loader.HttpLoader
import ps.isel.ipl.pt.gabageapp.util.loader.Result
import ps.isel.ipl.pt.gabageapp.util.loader.json_home.Resources
import ps.isel.ipl.pt.gabageapp.util.loader.siren.dto.SirenOccupationRangeDto
import ps.isel.ipl.pt.gabageapp.util.loader.v4.SirenOccupationCallBackV4


@SuppressLint("ValidFragment")
class ManageFragment(val resources: Resources) : Fragment() {

    private val LOADER_ID_0_50 = 1254
    private val LOADER_ID_50_75 = 9658
    private val LOADER_ID_75_100 = 8547
    private val REQUEST = "request"
    private val MAX = "{max}"
    private val MIN = "{min}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var href =resources.getOccupationRange.hrefTemplate
        var greenBundel = Bundle()
        var greenHttp = HttpLoader("${WasteManageApi.HOST_NAME_API}${href.replace(MIN,"0").replace(MAX,"50")}", arrayOf(Header("Accept","application/vnd.siren+json")))
        greenBundel.putParcelable(REQUEST,greenHttp)
        loaderManager.initLoader<Result<SirenOccupationRangeDto>>(LOADER_ID_0_50, greenBundel, object : SirenOccupationCallBackV4(context!!, REQUEST){
            override fun onSuccess(result: SirenOccupationRangeDto) {
                green_state_text.text = "${result.properties.occupationInRange}%"
            }
        })

        var yellowBundel = Bundle()
        var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${href.replace(MIN,"50").replace(MAX,"75")}", arrayOf(Header("Accept","application/vnd.siren+json")))
        yellowBundel.putParcelable(REQUEST,http)
        loaderManager.initLoader<Result<SirenOccupationRangeDto>>(LOADER_ID_50_75, yellowBundel, object : SirenOccupationCallBackV4(context!!, REQUEST){
            override fun onSuccess(result: SirenOccupationRangeDto) {
                yellow_state_text.text = "${result.properties.occupationInRange}%"
            }
        })

        var redBundel = Bundle()
        var redHttp = HttpLoader("${WasteManageApi.HOST_NAME_API}${href.replace(MIN,"75").replace(MAX,"100")}", arrayOf(Header("Accept","application/vnd.siren+json")))
        redBundel.putParcelable(REQUEST,redHttp)
        loaderManager.initLoader<Result<SirenOccupationRangeDto>>(LOADER_ID_75_100, redBundel, object : SirenOccupationCallBackV4(context!!, REQUEST){
            override fun onSuccess(result: SirenOccupationRangeDto) {
                red_state_text.text = "${result.properties.occupationInRange}%"
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_manage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        routes_button.setOnClickListener {
            val intent = Intent(activity, AllRoutesActivity::class.java)
            intent.putExtra(GET_ROUTES, resources.getRoutes.href)
            startActivity(intent)
        }

        trucks_button.setOnClickListener {
            val intent = Intent(activity, AllTrucksActivity::class.java)
            intent.putExtra(GET_TRUCKS, resources.getTrucks.href)
            startActivity(intent)
        }

        stations_button.setOnClickListener {
            val intent = Intent(activity, AllStationsActivity::class.java)
            intent.putExtra(GET_STATIONS, resources.getStations.href)
            startActivity(intent)
        }

        communications_button.setOnClickListener {
            val intent = Intent(activity, AllConfigurations::class.java)
            intent.putExtra(GET_CONFIGURATIONS, resources.getConfigurations.href)
            startActivity(intent)
        }
    }
}
