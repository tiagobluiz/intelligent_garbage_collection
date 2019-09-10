package ps.isel.ipl.pt.gabageapp

import android.content.Intent
import android.content.Loader
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MarkerOptions

import kotlinx.android.synthetic.main.activity_container.*
import kotlinx.android.synthetic.main.container_edit_config_dialog.view.*
import kotlinx.android.synthetic.main.container_edit_dialog.view.*
import kotlinx.android.synthetic.main.container_edit_location_dialog.view.*
import kotlinx.android.synthetic.main.content_container.*
import ps.isel.ipl.pt.gabageapp.WashesActivity.Companion.GET_WASHES
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi.Companion.HOST_NAME_API
import ps.isel.ipl.pt.gabageapp.service_web_api.service.ResulFromServiceErro
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterOptions
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Option
import ps.isel.ipl.pt.gabageapp.util.loader.siren.dto.SirenContainerDto
import android.text.InputType
import android.util.Log
import android.widget.TextView
import ps.isel.ipl.pt.gabageapp.ConfigurationActivity.Companion.GET_CONFIGURATIONS
import ps.isel.ipl.pt.gabageapp.ContainerCollects.Companion.GET_CONTAINER_COLLECTS
import ps.isel.ipl.pt.gabageapp.service.ServiceLocator


class ContainerActivity : AppCompatActivity(), OnMapReadyCallback, LoaderCallbacksError<SirenContainerDto> {

    companion object {
        val GET_CONTAINER = "getRoute"
    }

    private val TAG = "ContainerActivity"
    private lateinit var map:GoogleMap
    private val MAP_VIEW_BUNDLE_KEY = "MapContainerViewBundleKey"

    private val LOADER_ID = 2389
    private val REQUEST = "request"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        setSupportActionBar(toolbar)

        val url = intent.extras.getString(GET_CONTAINER)
        Log.i(TAG, "onCreate")
        Log.i(TAG, "Load Container")
        var bundel = Bundle()
        var http = HttpLoader("${HOST_NAME_API}$url", arrayOf(Header("Accept","application/vnd.siren+json")))
        bundel.putParcelable(REQUEST,http)
        loaderManager.initLoader(LOADER_ID, bundel, this@ContainerActivity)

        var mapViewBundle: Bundle? = null
        if(savedInstanceState !=null)
            mapViewBundle= savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        map_container.onCreate(mapViewBundle)
        map_container.getMapAsync(this)
    }



    override fun onMapReady(googleMap: GoogleMap) {
        Log.i(TAG, "onMapReady")
        map = googleMap
        map.setMinZoomPreference(14.0f);
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<SirenContainerDto>> {
        Log.i(TAG, "onCreateLoader")
        return AsyncTaskLoaderApi<SirenContainerDto>(
                SirenContainerDto::class.java,
                args.getParcelable(REQUEST),
                this@ContainerActivity
        )
    }

    override fun onSuccess(result: SirenContainerDto) {
        Log.i(TAG, "onSuccess")
        var container = result.toContainer()
        container_id_text.text = "${container.id}"
        container_type_text.text = container.type
        collect_zone_id_text.text = "${container.collectZoneId}"
        wash_number_text.text = "${container.numWash}"
        collect_number_text.text = "${container.numCollect}"
        occupation_text.text = "${container.occupation}%"
        battery_level_text.text = "${container.battery}%"
        temperature_text.text = "${container.temperature}ºC"
        iot_id_text.text = "${container.iotId}"
        configuration_id_text.text = "${container.configurationId}"
        container_type_text.text = container.type
        active_container_switch.isChecked = container.active

        map.addMarker(MarkerOptions().position(container.location))
        map.moveCamera(CameraUpdateFactory.newLatLng(container.location))

        val deactiveAction = result.actions.find { it.name.equals("deactivate-container") }
        val activeAction = result.actions.find { it.name.equals("activate-container") }
        if(deactiveAction!=null && activeAction!=null){
            active_container_switch.setOnCheckedChangeListener { compoundButton, isChecked ->
                if(isChecked) {
                    Log.i(TAG, "Container active")
                    ServiceLocator.getMakeRequest().startAction(this@ContainerActivity, activeAction, object : ResulFromServiceErro(this) {
                        override fun onSuccess(redirect: String?) {
                            Toast.makeText(this@ContainerActivity, "State : ${!container.active}", Toast.LENGTH_SHORT)
                        }

                    })
                }else {
                    Log.i(TAG, "Container deactive")
                    ServiceLocator.getMakeRequest().startAction(this@ContainerActivity, deactiveAction, object : ResulFromServiceErro(this) {
                        override fun onSuccess(redirect: String?) {
                            Toast.makeText(this@ContainerActivity, "State : ${!container.active}", Toast.LENGTH_SHORT)
                        }

                    })
                }
            }
        }

        val mBuilder = AlertDialog.Builder(this)
        val mViewEdit = layoutInflater.inflate(R.layout.container_edit_dialog, null)
        mBuilder.setView(mViewEdit)
        val dialog = mBuilder.create()

        val updateConfig = result.actions.find { it.name.equals("update-container-configuration") }
        val updateLocal = result.actions.find { it.name.equals("update-container-localization") }

        val configButton: ((v: View)->Unit) = {
            dialog.dismiss()
            Log.i(TAG, "Container edit switch dismiss")
            if(updateConfig != null) {
                val config = AlertDialog.Builder(this)
                val view = layoutInflater.inflate(R.layout.container_edit_config_dialog, null)
                config.setView(view)
                val configDialog = config.create()
                val typeOptions = updateConfig.fields.find { it.name.equals("containerType") }
                if(typeOptions !=null ) {
                    var options = arrayListOf<Option>()
                    val adapter = ArrayAdapterOptions(this,
                            R.layout.option_item,
                            typeOptions.options.toCollection(options))
                    adapter.setDropDownViewResource(R.layout.option_item);
                    view.type_options_edited.adapter = adapter
                }
                view.iot_id_edited.setText(container.iotId, TextView.BufferType.EDITABLE)
                view.height_edited.setText("${container.height}", TextView.BufferType.EDITABLE)
                view.configuration_id_edited.setText("${container.configurationId}", TextView.BufferType.EDITABLE)

                view.save_edited.setOnClickListener {
                    Log.i(TAG, "Container edit configuration request")
                    var iotId = updateConfig.fields.find { it.name.equals("iotId") }
                    var height = updateConfig.fields.find { it.name.equals("height") }
                    var configId =  updateConfig.fields.find { it.name.equals("configurationId") }
                    if (iotId != null && height != null && typeOptions != null && configId != null) {
                        iotId.value = view.iot_id_edited.text.toString()
                        height.value = view.height_edited.text.toString()
                        configId.value = view.configuration_id_edited.text.toString()
                        typeOptions.value = (view.type_options_edited.selectedItem as Option).value
                        ServiceLocator.getMakeRequest().startAction(this@ContainerActivity, updateConfig, object : ResulFromServiceErro(this) {
                            override fun onSuccess(redirect: String?) {
                                Toast.makeText(this@ContainerActivity, "Updated", Toast.LENGTH_LONG).show()
                            }
                        })
                    }
                    configDialog.dismiss()
                    Log.i(TAG, "Container configuration edit dialog dismiss")
                }
                configDialog.show()
                Log.i(TAG, "Container configuration edit dialog show")
            }
        }

        val localButton:( (v: View)->Unit) = {
            dialog.dismiss()
            Log.i(TAG, "Container edit switch dismiss")
            if(updateLocal != null) {
                val config = AlertDialog.Builder(this)
                val view = layoutInflater.inflate(R.layout.container_edit_location_dialog, null)
                view.latitude_edited.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL
                view.longitude_edited.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL
                config.setView(view)
                val configDialog = config.create()
                view.collect_zone_id_edited.setText("${container.collectZoneId}", TextView.BufferType.EDITABLE)
                view.latitude_edited.setText("${container.location.latitude}", TextView.BufferType.EDITABLE)
                view.longitude_edited.setText("${container.location.longitude}", TextView.BufferType.EDITABLE)

                view.save_container_edited.setOnClickListener {
                    Log.i(TAG, "Container edit localization request")
                    var collectZoneId = updateLocal.fields.find { it.name.equals("collectZoneId") }
                    var latitude = updateLocal.fields.find { it.name.equals("latitude") }
                    var longitude =  updateLocal.fields.find { it.name.equals("longitude") }
                    if (collectZoneId != null && latitude != null && longitude != null) {
                        collectZoneId.value = view.collect_zone_id_edited.text.toString()
                        latitude.value = view.latitude_edited.text.toString()
                        longitude.value = view.longitude_edited.text.toString()
                        ServiceLocator.getMakeRequest().startAction(this@ContainerActivity, updateLocal, object : ResulFromServiceErro(this) {
                            override fun onSuccess(redirect: String?) {
                                Toast.makeText(this@ContainerActivity, "Updated", Toast.LENGTH_LONG).show()
                            }
                        })
                    }
                    configDialog.dismiss()
                    Log.i(TAG, "Container localization edit dialog dismiss")
                }
                configDialog.show()
                Log.i(TAG, "Container localization edit dialog show")
            }
        }

        edit_button.setOnClickListener {
            mViewEdit.configuration_container_edited.setOnClickListener(configButton)
            mViewEdit.loaction_container_edited.setOnClickListener(localButton)
            dialog.show()
            Log.i(TAG, "Container edit switch show")
        }

        wash_number_image.setOnClickListener {
            val intent = Intent(this, WashesActivity::class.java)
            intent.putExtra(GET_WASHES, container.washesURL)
            startActivity(intent)
            Log.i(TAG, "WashesActivity launch")
        }
        collect_number_image.setOnClickListener {
            val intent = Intent(this, ContainerCollects::class.java)
            intent.putExtra(GET_CONTAINER_COLLECTS, container.collectsURL)
            startActivity(intent)
            Log.i(TAG, "ContainerCollects launch")
        }

        configuration_id_text.setOnClickListener {
            val intent = Intent(this, ConfigurationActivity::class.java)
            intent.putExtra(GET_CONFIGURATIONS, container.configurationsURL)
            startActivity(intent)
            Log.i(TAG, "ConfigurationActivity launch")
        }
    }

    override fun onLoaderReset(loader: Loader<Result<SirenContainerDto>>?) {
        Log.i(TAG, "onLoaderReset")
        loader?.stopLoading()
    }

    override fun onResume() {
        Log.i(TAG, "onResume")
        map_container.onResume()
        super.onResume()
    }

    override fun onPause() {
        Log.i(TAG, "onPause")
        super.onPause()
        map_container.onPause()
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        super.onDestroy()
        map_container.onDestroy()
    }

    override fun onLowMemory() {
        Log.i(TAG, "onLowMemory")
        super.onLowMemory()
        map_container.onLowMemory()
    }
}
