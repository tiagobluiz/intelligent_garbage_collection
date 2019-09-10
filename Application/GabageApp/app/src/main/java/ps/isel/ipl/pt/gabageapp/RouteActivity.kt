package ps.isel.ipl.pt.gabageapp

import android.content.Intent
import android.content.Loader
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*

import kotlinx.android.synthetic.main.content_route.*
import kotlinx.android.synthetic.main.edit_route_dialog.view.*
import ps.isel.ipl.pt.gabageapp.RouteCollectsActivity.Companion.GET_COLLECTS
import ps.isel.ipl.pt.gabageapp.ContainersActivity.Companion.GET_CONTAINERS
import ps.isel.ipl.pt.gabageapp.RouteCollectZonesActivity.Companion.GET_COLLECTZONES
import ps.isel.ipl.pt.gabageapp.RouteLandFillsActivity.Companion.GET_LANDFILL
import ps.isel.ipl.pt.gabageapp.model.RouteItem
import ps.isel.ipl.pt.gabageapp.service.ServiceLocator
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi
import ps.isel.ipl.pt.gabageapp.service_web_api.service.ResulFromServiceErro
import ps.isel.ipl.pt.gabageapp.util.direction_json.DirectionRequest
import ps.isel.ipl.pt.gabageapp.util.direction_json.PostDirection
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.loader.loaders_callback.CollectionJsonCallBack
import ps.isel.ipl.pt.gabageapp.util.loader.siren.dto.SirenRouteDto

class RouteActivity : AppCompatActivity(), OnMapReadyCallback , PostDirection, LoaderCallbacksError<SirenRouteDto>{

    companion object {
        val GET_ROUTE = "getRoute"
    }

    private val TAG = "RouteActivity"
    private lateinit var map:GoogleMap
    private val MAP_VIEW_BUNDLE_KEY = "MapRouteViewBundleKey"
    private val LOADER_ID = 2587
    private val LOADER_COLLECT_ZONES_ID = 3698
    private val REQUEST = "request"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)
        var route = intent.extras.get(GET_ROUTE) as RouteItem

        Log.i(TAG, "onCreate")
        Log.i(TAG, "Load Route")
        var bundel = Bundle()
        var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${route.self}", arrayOf(Header("Accept","application/vnd.siren+json")))
        bundel.putParcelable(REQUEST,http)
        loaderManager.initLoader<Result<SirenRouteDto>>(LOADER_ID, bundel, this@RouteActivity)

        var mapViewBundle: Bundle? = null
        if(savedInstanceState !=null)
            mapViewBundle= savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        route_map.onCreate(mapViewBundle)
        route_map.getMapAsync(this)

        //var routes = arrayListOf<LatLng>(route.startPoint.latlng, LatLng(38.62403731,-9.11632314), LatLng(38.62266265,-9.11550774), route.endPoint.latlng)
        //DirectionRequest(this).execute(routes)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<SirenRouteDto>> {
        Log.i(TAG, "onCreateLoader")
        return AsyncTaskLoaderApi<SirenRouteDto>(
                SirenRouteDto::class.java,
                args.getParcelable(REQUEST),
                this@RouteActivity
        )
    }

    override fun onSuccess(result: SirenRouteDto) {
        Log.i(TAG, "onSuccess")
        var route = result.toRoutes()

        start_point_text.text = route.startPoint.locationName
        end_point_text.text = route.endPoint.locationName
        route_id_text.text = "" + route.routId
        collect_number_text.text = "${route.numCollects}"
        collect_zone_number_text.text = "${route.numCollectZones}"
        containers_number_text.text = "${route.numContainers}"
        route_active_switch.isChecked = route.enable

        var bundel = Bundle()
        var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${route.collecZonesURL}", arrayOf(Header("Accept","application/vnd.collection+json")))
        bundel.putParcelable(REQUEST,http)
        loaderManager.initLoader(LOADER_COLLECT_ZONES_ID, bundel,
                object : CollectionJsonCallBack(this@RouteActivity,REQUEST) {
                    override fun onSuccess(result: CollectionJson) {
                        val routePoints = arrayListOf<LatLng>()
                        routePoints.add(route.startPoint.latlng)
                        val collectZones = result.toCollectZones().map { it.location }
                        routePoints.addAll(collectZones)
                        routePoints.add(route.endPoint.latlng)
                        DirectionRequest(this@RouteActivity).execute(routePoints)
                    }
                }
        )

        val deactiveAction = result.actions.find { it.name.equals("deactivate-route") }
        val activeAction = result.actions.find { it.name.equals("activate-route") }
        if(deactiveAction!=null && activeAction!=null){
            route_active_switch.setOnCheckedChangeListener { compoundButton, isChecked ->
                if(isChecked) {
                    Log.i(TAG, "Route active")
                    ServiceLocator.getMakeRequest().startAction(this@RouteActivity, activeAction, object : ResulFromServiceErro(this) {
                        override fun onSuccess(redirect: String?) {
                            Toast.makeText(this@RouteActivity, "State : ${!route.enable}", Toast.LENGTH_SHORT)
                        }

                    })
                }else {
                    Log.i(TAG, "Route deactive")
                    ServiceLocator.getMakeRequest().startAction(this@RouteActivity, deactiveAction, object : ResulFromServiceErro(this) {
                        override fun onSuccess(redirect: String?) {
                            Toast.makeText(this@RouteActivity, "State : ${!route.enable}", Toast.LENGTH_SHORT)
                        }

                    })
                }
            }
        }

        val updateAction = result.actions.find { it.name.equals("update-route")}
        if(updateAction != null){
            edit_button.setOnClickListener {
                val mBuilder = AlertDialog.Builder(this)
                val mViewEdit = layoutInflater.inflate(R.layout.edit_route_dialog, null)
                mBuilder.setView(mViewEdit)
                val dialog =mBuilder.create()
                mViewEdit.start_point_edited.setText("${route.startPoint.latlng.latitude}|${route.startPoint.latlng.longitude}", TextView.BufferType.EDITABLE)
                mViewEdit.end_point_edited.setText("${route.endPoint.latlng.latitude}|${route.endPoint.latlng.longitude}", TextView.BufferType.EDITABLE)
                mViewEdit.save_route_edited.setOnClickListener {
                    Log.i(TAG, "Route edit request")
                    var start = updateAction.fields.find { it.name.equals("startPoint") }
                    var end = updateAction.fields.find { it.name.equals("finishPoint") }
                    if(start !=null && end != null){
                        start.value = mViewEdit.start_point_edited.text.toString()
                        end.value = mViewEdit.end_point_edited.text.toString()
                        ServiceLocator.getMakeRequest().startAction(this@RouteActivity, updateAction, object : ResulFromServiceErro(this) {
                            override fun onSuccess(redirect: String?) {
                                Toast.makeText(this@RouteActivity, "Updated", Toast.LENGTH_LONG).show()
                            }
                        })
                    }
                    dialog.dismiss()
                    Log.i(TAG, "Route edit dialog dismiss")
                }
                dialog.show()
                Log.i(TAG, "Route edit dialog show")
            }
        }

        collect_zone_button.setOnClickListener {
            val intent = Intent(this, RouteCollectZonesActivity::class.java)
            intent.putExtra(GET_COLLECTZONES, route.collecZonesURL)
            startActivity(intent)
            Log.i(TAG, "RouteCollectZonesActivity launch")
        }

        containers_button.setOnClickListener {
            val intent = Intent(this, RouteContainerActivity::class.java)
            intent.putExtra(GET_CONTAINERS, route.containersURL)
            startActivity(intent)
            Log.i(TAG, "RouteContainerActivity launch")
        }

        collect_button.setOnClickListener {
            val intent = Intent(this, RouteCollectsActivity::class.java)
            intent.putExtra(GET_COLLECTS, route.collectsURL)
            startActivity(intent)
            Log.i(TAG, "RouteCollectsActivity launch")
        }

        landfill_button.setOnClickListener {
            val intent = Intent(this, RouteLandFillsActivity::class.java)
            intent.putExtra(GET_LANDFILL, route.dropZonesURL)
            startActivity(intent)
            Log.i(TAG, "RouteLandFillsActivity launch")
        }
    }

    override fun onLoaderReset(loader: Loader<Result<SirenRouteDto>>?) {
        loader?.stopLoading()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.i(TAG, "onLoaderReset")
        map = googleMap
        map.setMinZoomPreference(14.0f)
    }

    override fun onDirectionFinderSuccess(markers: List<LatLng>, route: List<LatLng>) {
        Log.i(TAG, "onDirectionFinderSuccess")
        map.addPolyline(PolylineOptions().addAll(route))
        markers.forEach { map.addMarker(MarkerOptions().position(it)) }
        map.moveCamera(CameraUpdateFactory.newLatLng(markers.first()))

        start_point_button.setOnClickListener {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(markers.first(),18.0f))
        }
        end_point_button.setOnClickListener {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(markers.last(),18.0f))
        }
    }

    override fun onResume() {
        Log.i(TAG, "onResume")
        route_map.onResume()
        super.onResume()
    }

    override fun onPause() {
        Log.i(TAG, "onPause")
        super.onPause()
        route_map.onPause()
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        super.onDestroy()
        route_map.onDestroy()
    }

    override fun onLowMemory() {
        Log.i(TAG, "onLowMemory")
        super.onLowMemory()
        route_map.onLowMemory()
    }

}
