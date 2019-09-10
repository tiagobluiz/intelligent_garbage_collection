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
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.collect_zone_edit_dialog.view.*

import kotlinx.android.synthetic.main.content_collect_zone.*
import ps.isel.ipl.pt.gabageapp.ContainersActivity.Companion.GET_CONTAINERS
import ps.isel.ipl.pt.gabageapp.service.ServiceLocator
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi
import ps.isel.ipl.pt.gabageapp.service_web_api.service.ResulFromServiceErro
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.loader.loaders_callback.CollectionJsonCallBack
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Option
import ps.isel.ipl.pt.gabageapp.util.loader.siren.dto.SirenCollectZoneDto
import ps.isel.ipl.pt.gabageapp.util.scroll.EndlessScrollListener

class CollectZoneActivity : AppCompatActivity(), OnMapReadyCallback, LoaderCallbacksError<SirenCollectZoneDto> {

    companion object {
        val GET_COLLECTZONE = "getCollectZone"
    }

    private val TAG = "CollectZoneActivity"
    private lateinit var map:GoogleMap
    private val MAP_VIEW_BUNDLE_KEY = "MapCollectZoneViewBundleKey"
    private val REQUEST = "request"
    private val LOADER_ID = 5652
    private val LOADER_CONTAINERS_ID = 1294

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect_zone)

        val url = intent.extras.getString(GET_COLLECTZONE)
        Log.i(TAG, "onCreate")
        Log.i(TAG, "Load CollectZone")
        var bundel = Bundle()
        var http = HttpLoader("${WasteManageApi.HOST_NAME_API}$url", arrayOf(Header("Accept","application/vnd.siren+json")))
        bundel.putParcelable(REQUEST,http)
        loaderManager.initLoader<Result<SirenCollectZoneDto>>(LOADER_ID, bundel, this@CollectZoneActivity)

        var mapViewBundle: Bundle? = null
        if(savedInstanceState !=null)
            mapViewBundle= savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        collect_zone_map.onCreate(mapViewBundle)
        collect_zone_map.getMapAsync(this)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<SirenCollectZoneDto>> {
        Log.i(TAG, "onCreateLoader")
        return AsyncTaskLoaderApi<SirenCollectZoneDto>(
                SirenCollectZoneDto::class.java,
                args.getParcelable(REQUEST),
                this@CollectZoneActivity
        )
    }

    override fun onSuccess(result: SirenCollectZoneDto) {
        Log.i(TAG, "onSuccess")
        val collectZone = result.toCollectZone()

        latitude_text.text = "${collectZone.location.latitude}"
        longitude_text.text = "${collectZone.location.longitude}"

        num_containers_text.text = "${collectZone.numContainers}"
        collect_zone_id_text.text = "${collectZone.collectZoneId}"
        general_occupation_text.text = "${collectZone.generalOccupation}%"
        paper_occupation_text.text = "${collectZone.paperOccupation}%"
        plastic_occupation_text.text = "${collectZone.plasticOccupation}%"
        glass_occupation_text.text = "${collectZone.glassOccupation}%"
        active_zollect_zone.isChecked = collectZone.active


        val deactiveAction = result.actions.find { it.name.equals("deactivate-collect-zone") }
        val activeAction = result.actions.find { it.name.equals("activate-collect-zone") }
        if(deactiveAction!=null && activeAction !=null){
            active_zollect_zone.setOnCheckedChangeListener { compoundButton, isChecked ->
                if(isChecked) {
                    Log.i(TAG, "CollectZone active")
                    ServiceLocator.getMakeRequest().startAction(this@CollectZoneActivity, activeAction, object : ResulFromServiceErro(this) {
                        override fun onSuccess(redirect: String?) {
                            Toast.makeText(this@CollectZoneActivity, "State : ${!collectZone.active}", Toast.LENGTH_SHORT)
                        }

                    })
                }else {
                    Log.i(TAG, "CollectZone deactive")
                    ServiceLocator.getMakeRequest().startAction(this@CollectZoneActivity, deactiveAction, object : ResulFromServiceErro(this) {
                        override fun onSuccess(redirect: String?) {
                            Toast.makeText(this@CollectZoneActivity, "State : ${!collectZone.active}", Toast.LENGTH_SHORT)
                        }

                    })
                }
            }
        }

        val update = result.actions.find { it.name.equals("update-collect-zone") }
        if(update!=null){
            edit_button.setOnClickListener {
                val mBuilder = AlertDialog.Builder(this)
                val mViewEdit = layoutInflater.inflate(R.layout.collect_zone_edit_dialog, null)
                mBuilder.setView(mViewEdit)
                val dialog =mBuilder.create()
                mViewEdit.route_id_edited.setText("${collectZone.routeId}", TextView.BufferType.EDITABLE)
                mViewEdit.save_collect_zone_edited.setOnClickListener {
                    Log.i(TAG, "CollectZone edit request")
                    val fieldRouteId = update.fields.find {  it.name.equals("routeId")}
                    if(fieldRouteId!=null){
                        fieldRouteId.value = mViewEdit.route_id_edited.text.toString()
                        ServiceLocator.getMakeRequest().startAction(this@CollectZoneActivity, update, object : ResulFromServiceErro(this) {
                            override fun onSuccess(redirect: String?) {
                                Toast.makeText(this@CollectZoneActivity, "Updated", Toast.LENGTH_LONG).show()
                            }
                        })
                    }
                    dialog.dismiss()
                    Log.i(TAG, "CollectZone edit dialog dismiss")
                }
                dialog.show()
                Log.i(TAG, "CollectZone edit dialog show")
            }
        }

        var bundel = Bundle()
        var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${collectZone.containerUrl}", arrayOf(Header("Accept","application/vnd.collection+json")))
        bundel.putParcelable(REQUEST,http)
        loaderManager.initLoader<Result<CollectionJson>>(LOADER_CONTAINERS_ID, bundel, object : CollectionJsonCallBack(this@CollectZoneActivity,REQUEST) {
            override fun onSuccess(result: CollectionJson) {
                val containers = result.toContainers()
                containers.map { map.addMarker(MarkerOptions().position(it.location)) }
                map.moveCamera(CameraUpdateFactory.newLatLng(containers.first().location))
            }
        })

        containers_button.setOnClickListener {
            var intent = Intent(this, CollectZoneContainerActivity::class.java)
            intent.putExtra(GET_CONTAINERS, collectZone.containerUrl)
            startActivity(intent)
            Log.i(TAG, "CollectZoneActivity launch")
        }
    }

    override fun onLoaderReset(loader: Loader<Result<SirenCollectZoneDto>>?) {
        Log.i(TAG, "onLoaderReset")
        loader?.stopLoading()
    }

    override fun onMapReady(mMap: GoogleMap) {
        Log.i(TAG, "onMapReady")
        map = mMap
        map.setMinZoomPreference(14.0f)
    }

    override fun onResume() {
        Log.i(TAG, "onResume")
        collect_zone_map.onResume()
        super.onResume()
    }

    override fun onPause() {
        Log.i(TAG, "onPause")
        super.onPause()
        collect_zone_map.onPause()
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        super.onDestroy()
        collect_zone_map.onDestroy()
    }

    override fun onLowMemory() {
        Log.i(TAG, "onLowMemory")
        super.onLowMemory()
        collect_zone_map.onLowMemory()
    }
}
