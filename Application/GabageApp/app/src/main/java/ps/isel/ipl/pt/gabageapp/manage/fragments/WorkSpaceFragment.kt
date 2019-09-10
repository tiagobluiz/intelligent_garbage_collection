package ps.isel.ipl.pt.gabageapp.manage.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_work_space.*
import ps.isel.ipl.pt.gabageapp.CollectZoneActivity
import ps.isel.ipl.pt.gabageapp.R
import ps.isel.ipl.pt.gabageapp.model.CollectZoneItem
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi
import ps.isel.ipl.pt.gabageapp.util.custom_info.CollectInfoWindow
import ps.isel.ipl.pt.gabageapp.util.custom_info.CollectZonesInfoWindow
import ps.isel.ipl.pt.gabageapp.util.loader.Header
import ps.isel.ipl.pt.gabageapp.util.loader.HttpLoader
import ps.isel.ipl.pt.gabageapp.util.loader.Result
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.loader.json_home.Resources
import ps.isel.ipl.pt.gabageapp.util.loader.toCollectZones
import ps.isel.ipl.pt.gabageapp.util.loader.v4.AsyncTaskLoaderApiV4
import ps.isel.ipl.pt.gabageapp.util.loader.v4.LoaderCallbacksErrorV4


@SuppressLint("ValidFragment")
class WorkSpaceFragment(val resources: Resources) : Fragment(), OnMapReadyCallback, LoaderCallbacksErrorV4<CollectionJson> {

    private lateinit var mMap:GoogleMap
    private val MAP_VIEW_BUNDLE_KEY = "MapManageViewBundleKey"
    private val LOADER_ID = 4589
    private val REQUEST = "request"
    private val RANGE = 1000
    private val MIN_TIME: Long = 1000
    private val MIN_DISTANCE = 250.0f
    private val listCollectZones = arrayListOf<CollectZoneItem>()
    private val markersShow = arrayListOf<Marker>()
    private  lateinit var locationListener: LocationListener
    private lateinit var locationService : LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_work_space, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var mapViewBundle: Bundle? = null
        if(savedInstanceState !=null)
            mapViewBundle= savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)

        mapManage.onCreate(mapViewBundle)
        mapManage.getMapAsync(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        if(mapViewBundle==null){
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapManage.onSaveInstanceState(mapViewBundle)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMinZoomPreference(14.0f)
        mMap.setInfoWindowAdapter(CollectZonesInfoWindow(activity!!))
        mMap.setOnInfoWindowClickListener {
            val collectZone = it.tag as CollectZoneItem
            var intent = Intent(activity!!, CollectZoneActivity::class.java)
            intent.putExtra(CollectZoneActivity.GET_COLLECTZONE,collectZone.self)
            ContextCompat.startActivity(activity!!, intent, null)

        }

        if (activity!= null && ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationService = activity!!.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude,location.longitude)))
                    var bundel = Bundle()
                    val href = resources.getCollectInRange.href.replace("{latitude}", "${location.latitude}").replace("{longitude}", "${location.longitude}").replace("{range}", "${RANGE}")
                    var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${href}", arrayOf(Header("Accept","application/vnd.collection+json")))
                    bundel.putParcelable(REQUEST,http)
                    if(listCollectZones.size==0)
                        loaderManager.initLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@WorkSpaceFragment)
                    else
                        loaderManager.restartLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@WorkSpaceFragment)
                }
                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener, null)
        }
    }

    override fun onSuccess(result: CollectionJson) {
        val collectZonesOnRange = result.toCollectZones()

        val oldInRange= arrayListOf<CollectZoneItem>()
        oldInRange.addAll(listCollectZones)
        oldInRange.removeAll(collectZonesOnRange.intersect(listCollectZones))
        listCollectZones.removeAll(oldInRange)

        val newInRange = arrayListOf<CollectZoneItem>()
        newInRange.addAll(collectZonesOnRange)
        newInRange.removeAll(listCollectZones.intersect(collectZonesOnRange))
        listCollectZones.addAll(newInRange)

        val newMarkers = newInRange.map {
            val marker = mMap.addMarker(MarkerOptions().position(LatLng(it.location.latitude, it.location.longitude)))
            marker.tag = it
            marker
        }
        val oldMarkers = markersShow.filter { oldInRange.contains(it.tag) }
        markersShow.addAll(newMarkers)
        markersShow.removeAll(oldMarkers)
        oldMarkers.forEach {
            it.remove()
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<CollectionJson>> {
        return AsyncTaskLoaderApiV4<CollectionJson>(
                CollectionJson::class.java,
                args.getParcelable(REQUEST),
                context!!
        )
    }

    override fun onLoaderReset(loader: Loader<Result<CollectionJson>>?) {
        loader?.reset()
    }

    override fun onResume() {
        mapManage.onResume()
        super.onResume()
    }

    override fun onPause() {
        mapManage.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        mapManage.onDestroy()
        super.onDestroyView()
        locationService.removeUpdates(locationListener)
    }

    override fun onLowMemory() {
        mapManage.onLowMemory()
        super.onLowMemory()
    }
}
