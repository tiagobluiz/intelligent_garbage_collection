package ps.isel.ipl.pt.gabageapp

import android.Manifest
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map_employee_work.*
import kotlinx.android.synthetic.main.app_bar_map_employee_work.*
import kotlinx.android.synthetic.main.collect_route_dialog_init.view.*
import kotlinx.android.synthetic.main.content_map_employee_work.*
import ps.isel.ipl.pt.gabageapp.service_web_api.service.ResulFromServiceErro
import ps.isel.ipl.pt.gabageapp.util.custom_info.CustomInfo
import ps.isel.ipl.pt.gabageapp.util.custom_info.CollectInfoWindow
import ps.isel.ipl.pt.gabageapp.util.loader.json_home.HomeJson
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Action
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Field
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Option
import java.util.*
import android.content.pm.PackageManager
import android.location.LocationManager.*
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.collect_or_wash_dialog.view.*
import kotlinx.android.synthetic.main.nav_header_map_employee_work.view.*
import ps.isel.ipl.pt.gabageapp.service.ServiceLocator
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterOptions
import ps.isel.ipl.pt.gabageapp.util.direction_json.DirectionRequest
import ps.isel.ipl.pt.gabageapp.util.direction_json.PostDirection
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.loader.loaders_callback.CollectionJsonCallBack
import ps.isel.ipl.pt.gabageapp.util.loader.siren.dto.SirenUserDto
import ps.isel.ipl.pt.gabageapp.util.loader.LoaderCallbacksError
import ps.isel.ipl.pt.gabageapp.util.loader.siren.dto.SirenRouteCollectDto
import ps.isel.ipl.pt.gabageapp.util.loader.siren.dto.SirenRouteDto


class MapEmployeeWork : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, PostDirection {

    companion object {
        val GET_HOME_MAP = "getHome"
    }

    private lateinit var mMap:GoogleMap
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    private lateinit var home : HomeJson
    private val REQUEST = "request"
    private val LOADER_PROFILE_ID = 6298
    private val LOADER_CONTAINERS_ID = 6298
    private val LOADER_ROUTE_ID = 1254
    private val LOADER_PLAN_ID = 8965
    private val TAG = "MapEmployeeWork"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_employee_work)
        setSupportActionBar(toolbar)

        home = intent.extras.get(GET_HOME_MAP) as HomeJson

        val collectInit = home.resources.getCollectRoute

        init_collect_button.setOnClickListener { view ->
            val mBuilder = AlertDialog.Builder(this)
            val mViewEdit = layoutInflater.inflate(R.layout.collect_route_dialog_init, null)
            mBuilder.setView(mViewEdit)
            val dialog =mBuilder.create()
            val truckPlate = Field("truckPlate","","","", arrayOf<Option>())
            val startDate = Field("startDate","","","", arrayOf<Option>())
            val containerType = Field("containerType","","","", arrayOf<Option>())
            val latitude = Field("latitude","","","", arrayOf<Option>())
            val longitude = Field("longitude","","","", arrayOf<Option>())
            val fields = arrayListOf<Field>(truckPlate, startDate, containerType, latitude, longitude)
            val adapter = ArrayAdapterOptions(this,
                    R.layout.option_item,
                    collectInit.hints.formats.json.containerTypeOptions)
            adapter.setDropDownViewResource(R.layout.option_item);
            mViewEdit.type_to_collect_spinner.adapter = adapter
            val init = Action("","",collectInit.hints.allow.first(),collectInit.hrefTemplate, collectInit.hints.acceptPost.first(),fields)

            mViewEdit.init_collect_button.setOnClickListener {
                truckPlate.value = mViewEdit.truck_to_collect.text.toString()
                startDate.value = formatter.format(Date())
                containerType.value = (mViewEdit.type_to_collect_spinner.selectedItem as Option).value
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    val loc = getSystemService(LOCATION_SERVICE) as LocationManager
                    loc.requestSingleUpdate(NETWORK_PROVIDER, object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            latitude.value = "${location.latitude}"
                            longitude.value = "${location.longitude}"
                            ServiceLocator.getMakeRequest().startAction(this@MapEmployeeWork, init, object : ResulFromServiceErro(this@MapEmployeeWork) {
                                override fun onSuccess(redirect: String?) {
                                    if(redirect != null) {
                                        createdCollect(redirect, containerType.value)
                                        init_collect_button.visibility = View.GONE
                                        Toast.makeText(this@MapEmployeeWork, "Started", Toast.LENGTH_LONG).show()
                                    }
                                }
                            })
                            loc.removeUpdates(this)
                            dialog.dismiss()
                        }

                        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }, null)
                }
            }
            dialog.show()
        }

        val toggle = object : ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            override fun onDrawerOpened(drawerView: View) {
                var bundel = Bundle()
                var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${home.resources.getCurrentEmployee.href}", arrayOf(Header("Accept","application/vnd.siren+json")))
                bundel.putParcelable(REQUEST,http)
                loaderManager.initLoader<Result<SirenUserDto>>(LOADER_PROFILE_ID, bundel, object :  LoaderCallbacksError<SirenUserDto>{
                    override fun onSuccess(result: SirenUserDto) {
                        val collector = result.toUser()
                        drawerView.user_email_text.text = collector.email
                        drawerView.user_phone_text.text = "${collector.phoneNumber}"
                        drawerView.username.text = collector.username
                        drawerView.user_office_text.text = collector.post
                    }

                    override fun onCreateLoader(id: Int, args: Bundle): android.content.Loader<Result<SirenUserDto>> {
                        return AsyncTaskLoaderApi<SirenUserDto>(
                                SirenUserDto::class.java,
                                args.getParcelable(REQUEST),
                                this@MapEmployeeWork
                        )
                    }

                    override fun onLoaderReset(loader: android.content.Loader<Result<SirenUserDto>>?) {
                        loader?.stopLoading()
                    }
                })
                super.onDrawerOpened(drawerView)
            }
        }
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        var mapViewBundle: Bundle? = null
        if(savedInstanceState !=null)
            mapViewBundle= savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)

        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        nav_view.setNavigationItemSelectedListener(this)
    }

    fun createdCollect(redirect: String, type: String){
        var bundel = Bundle()
        var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${redirect}", arrayOf(Header("Accept","application/vnd.siren+json")))
        bundel.putParcelable(REQUEST,http)
        loaderManager.initLoader<Result<SirenRouteCollectDto>>(
                LOADER_CONTAINERS_ID,
                bundel,
                object :  LoaderCallbacksError<SirenRouteCollectDto>{
                    override fun onSuccess(result: SirenRouteCollectDto) {
                        val routeCollect = result.toRouteCollect()
                        finish_collect_button.setOnClickListener {
                            val updateCollect =result.actions.find { it.name.equals("update-route-collection")}
                            if(updateCollect != null){
                                val truckPlane = updateCollect.fields.find { it.name.equals("truckPlate")}
                                val finishDate = updateCollect.fields.find { it.name.equals("finishDate")}
                                if(finishDate != null && truckPlane != null) {
                                    finishDate.value = formatter.format(Date())
                                    truckPlane.value = routeCollect.truckPlate
                                    ServiceLocator.getMakeRequest().startAction(this@MapEmployeeWork, updateCollect, object : ResulFromServiceErro(this@MapEmployeeWork) {
                                        override fun onSuccess(redirect: String?) {
                                            Toast.makeText(this@MapEmployeeWork, "Terminated", Toast.LENGTH_LONG).show()
                                            init_collect_button.visibility = View.VISIBLE
                                            show_on_maps_buttom.visibility = View.GONE
                                            finish_collect_button.visibility = View.GONE
                                            mMap.clear()
                                        }
                                    })
                                }
                            }
                        }
                        getRoute(routeCollect.upUrl, type)
                    }
                    override fun onCreateLoader(id: Int, args: Bundle): android.content.Loader<Result<SirenRouteCollectDto>> {
                        return AsyncTaskLoaderApi<SirenRouteCollectDto>(
                                SirenRouteCollectDto::class.java,
                                args.getParcelable(REQUEST),
                                this@MapEmployeeWork
                        )
                    }
                    override fun onLoaderReset(loader: android.content.Loader<Result<SirenRouteCollectDto>>?) {
                        loader?.stopLoading()
                    }
                }
        )
    }

    fun getRoute(routeUrl: String, type: String){
        var bundel = Bundle()
        var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${routeUrl}", arrayOf(Header("Accept","application/vnd.siren+json")))
        bundel.putParcelable(REQUEST,http)
        loaderManager.initLoader<Result<SirenRouteDto>>(
                LOADER_ROUTE_ID,
                bundel,
                object :  LoaderCallbacksError<SirenRouteDto>{
                    override fun onSuccess(result: SirenRouteDto) {
                        val route = result.toRoutes()
                        getPlan("${route.planURL}?type=$type", type, route.startPoint.latlng, route.endPoint.latlng)
                    }
                    override fun onCreateLoader(id: Int, args: Bundle): android.content.Loader<Result<SirenRouteDto>> {
                        return AsyncTaskLoaderApi<SirenRouteDto>(
                                SirenRouteDto::class.java,
                                args.getParcelable(REQUEST),
                                this@MapEmployeeWork
                        )
                    }
                    override fun onLoaderReset(loader: android.content.Loader<Result<SirenRouteDto>>?) {
                        loader?.stopLoading()
                    }
                }
        )
    }

    fun getPlan(routePlan: String, type: String, startPoint: LatLng, endpoint: LatLng){
        var bundel = Bundle()
        var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${routePlan}", arrayOf(Header("Accept","application/vnd.collection+json")))
        bundel.putParcelable(REQUEST,http)
        loaderManager.initLoader<Result<CollectionJson>>(
                LOADER_PLAN_ID,
                bundel,
                object : CollectionJsonCallBack(this@MapEmployeeWork,REQUEST) {
                    override fun onSuccess(result: CollectionJson) {
                        val collectZones = result.toCollectZones()
                        collectZones.forEach {
                            val collectZoneItem = it
                            val market = mMap.addMarker(MarkerOptions().position(it.location))
                            val info = CustomInfo(collectZoneItem.id)
                            info.collectListener = {
                                if(collectZoneItem.collectURI != null) {
                                    val collectDate = Field("collectDate","",formatter.format(Date()),"", arrayOf<Option>())
                                    val containerType = Field("containerType","",type,"", arrayOf<Option>())
                                    val fields = arrayListOf<Field>(collectDate, containerType)
                                    val collectAction = Action("", "", "POST", collectZoneItem.collectURI!!, "application/json", fields)
                                    ServiceLocator.getMakeRequest().startAction(this@MapEmployeeWork, collectAction, object : ResulFromServiceErro(this@MapEmployeeWork) {
                                        override fun onSuccess(redirect: String?) {
                                            Toast.makeText(this@MapEmployeeWork, "Collected", Toast.LENGTH_LONG).show()
                                        }
                                    })
                                }
                            }

                            info.washListener = {
                                if(collectZoneItem.washURI != null) {
                                    val washDate = Field("washDate","",formatter.format(Date()),"", arrayOf<Option>())
                                    val containerType = Field("containerType","",type,"", arrayOf<Option>())
                                    val fields = arrayListOf<Field>(washDate, containerType)
                                    val washAction = Action("", "", "POST", collectZoneItem.washURI!!, "application/json", fields)
                                    ServiceLocator.getMakeRequest().startAction(this@MapEmployeeWork, washAction, object : ResulFromServiceErro(this@MapEmployeeWork) {
                                        override fun onSuccess(redirect: String?) {
                                            Toast.makeText(this@MapEmployeeWork, "Wahed", Toast.LENGTH_LONG).show()
                                        }
                                    })
                                }
                            }
                            market.tag=info
                        }
                        val collectZonesLatLong  = collectZones.map { it.location }
                        val allPlan = arrayListOf<LatLng>()
                        mMap.addMarker(MarkerOptions().position(startPoint))
                        mMap.addMarker(MarkerOptions().position(endpoint))
                        allPlan.add(startPoint)
                        allPlan.addAll(collectZonesLatLong)
                        allPlan.add(endpoint)
                        DirectionRequest(this@MapEmployeeWork).execute(allPlan)
                        show_on_maps_buttom.setOnClickListener {
                            val gmmIntentUri = Uri.parse(makeURItoGoogleMaps(allPlan))
                            val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                            startActivity(intent)
                        }
                        show_on_maps_buttom.visibility = View.VISIBLE
                    }
                }
        )
    }

    fun makeURItoGoogleMaps(route: ArrayList<LatLng>): String{
        var mapsUri = "https://www.google.com/maps/dir/?api=1&%s&%s&%s&travelmode=driving&dir_action=navigate"
        var origin = "origin=%s"
        var dest = "destination=%s"
        var waypoint = "waypoints=%s"

        var start = route.first()
        origin = String.format(origin, "${start.latitude},${start.longitude}")
        route.remove(start)

        var end = route.last()
        dest = String.format(dest, "${end.latitude},${end.longitude}")
        route.remove(end)

        var way = ""
        route.forEachIndexed{idx, item->
            way += "${item.latitude},${item.longitude}"
            if(idx!=route.size-1)
                way += "|"
        }
        waypoint = String.format(waypoint, way)
        return String.format(mapsUri, origin, dest, waypoint)
    }

    override fun onDirectionFinderSuccess(markers: List<LatLng>, route: List<LatLng>) {
        Log.i(TAG, "onDirectionFinderSuccess")
        mMap.addPolyline(PolylineOptions().addAll(route))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(markers.first()))
        finish_collect_button.visibility = View.VISIBLE

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState!!.getBundle(MAP_VIEW_BUNDLE_KEY)
        if(mapViewBundle==null){
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMinZoomPreference(14.0f)
        mMap.setInfoWindowAdapter(CollectInfoWindow(this@MapEmployeeWork))
        mMap.setOnInfoWindowClickListener {
            val info = it.tag as CustomInfo
            val mBuilder = AlertDialog.Builder(this)
            val mViewEdit = layoutInflater.inflate(R.layout.collect_or_wash_dialog, null)
            mBuilder.setView(mViewEdit)
            val dialog = mBuilder.create()
            mViewEdit.collect_button.setOnClickListener{
                info.collectListener(it)
                dialog.dismiss()
            }
            mViewEdit.wash_button.setOnClickListener{
                info.washListener(it)
                dialog.dismiss()
            }
            dialog.show()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val loc = getSystemService(LOCATION_SERVICE) as LocationManager
            loc.requestSingleUpdate(NETWORK_PROVIDER, object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude),18.0f))
                    loc.removeUpdates(this)
                }
                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }, null)
        }
    }

    public override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
