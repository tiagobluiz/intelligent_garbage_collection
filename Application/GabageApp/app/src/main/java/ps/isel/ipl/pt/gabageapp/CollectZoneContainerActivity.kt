package ps.isel.ipl.pt.gabageapp

import android.content.Loader
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_containers.*
import kotlinx.android.synthetic.main.add_new_container_dialog.view.*
import kotlinx.android.synthetic.main.collect_route_dialog_init.view.*
import kotlinx.android.synthetic.main.content_containers.*
import ps.isel.ipl.pt.gabageapp.model.CollectZoneItem
import ps.isel.ipl.pt.gabageapp.model.ContainerItem
import ps.isel.ipl.pt.gabageapp.service.ServiceLocator
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi
import ps.isel.ipl.pt.gabageapp.service_web_api.service.ResulFromServiceErro
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterCollectZoneContainers
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterCommunications
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterOptions
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Action
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Field
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Option
import ps.isel.ipl.pt.gabageapp.util.loader.siren.dto.SirenContainerDto

/**
 * Created by goncalo on 24/04/2018.
 */
class CollectZoneContainerActivity : ContainersActivity() {

    private val REQUEST = "request"
    private val LOADER_ID = 1967
    private val TAG = "CollectZoneContainers"

    override fun inititialize(list: ArrayList<ContainerItem>) {
        containers_list_view.adapter = ArrayAdapterCollectZoneContainers(this, R.layout.container_item, list)
        notifyChange()
    }

    override fun notifyChange() {
        (containers_list_view.adapter as ArrayAdapterCollectZoneContainers).notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        new_container_action_button.visibility = View.VISIBLE
    }

    override fun onLoadFinished(loader: Loader<Result<CollectionJson>>?, data: Result<CollectionJson>) {
        super.onLoadFinished(loader, data)
        if(data.result != null) {
            new_container_action_button.setOnClickListener {
                val mBuilder = AlertDialog.Builder(this)
                val mViewEdit = layoutInflater.inflate(R.layout.add_new_container_dialog, null)
                mBuilder.setView(mViewEdit)
                val dialog = mBuilder.create()
                val addnewContainer = data.result.collection.template
                val iotId = Field("iotId", "", "", "", arrayOf<Option>())
                val latitude = Field("latitude", "", "", "", arrayOf<Option>())
                val longitude = Field("longitude", "", "", "", arrayOf<Option>())
                val height = Field("height", "", "", "", arrayOf<Option>())
                val containerType = Field("containerType", "", "", "", arrayOf<Option>())
                val configurationId = Field("configurationId", "", "", "", arrayOf<Option>())
                val fields = arrayListOf<Field>(iotId, latitude, longitude, height, containerType, configurationId)
                val options = addnewContainer.data.find { it.name.equals("containerType") }!!.options
                val adapter = ArrayAdapterOptions(this,
                        R.layout.option_item,
                        options)
                adapter.setDropDownViewResource(R.layout.option_item);
                mViewEdit.new_container_type_options.adapter = adapter
                val createNewContainer = Action("", "", "POST", data.result.collection.href, "application/json", fields)
                mViewEdit.new_container_button.setOnClickListener {
                    iotId.value = mViewEdit.new_container_iot_id.text.toString()
                    latitude.value = mViewEdit.new_container_latitude.text.toString()
                    longitude.value = mViewEdit.new_container_longitude.text.toString()
                    height.value = mViewEdit.new_container_height.text.toString()
                    containerType.value = (mViewEdit.new_container_type_options.selectedItem as Option).value
                    configurationId.value = mViewEdit.new_container_configuration_id.text.toString()
                    ServiceLocator.getMakeRequest().startAction(this@CollectZoneContainerActivity, createNewContainer, object : ResulFromServiceErro(this@CollectZoneContainerActivity) {
                        override fun onSuccess(redirect: String?) {
                            if (redirect != null) {
                                var bundel = Bundle()
                                var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${redirect}", arrayOf(Header("Accept","application/vnd.siren+json")))
                                bundel.putParcelable(REQUEST,http)
                                loaderManager.initLoader<Result<SirenContainerDto>>(LOADER_ID, bundel, object: LoaderCallbacksError<SirenContainerDto> {
                                    override fun onSuccess(result: SirenContainerDto) {
                                        val container = result.toContainer()
                                        addNewItem(ContainerItem(container.id, container.temperature, container.occupation, container.battery, container.location, container.collectZoneId, container.active, redirect))
                                    }

                                    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<SirenContainerDto>> {
                                        Log.i(TAG, "onCreateLoaderConfiguration")
                                        return AsyncTaskLoaderApi<SirenContainerDto>(
                                                SirenContainerDto::class.java,
                                                args.getParcelable(REQUEST),
                                                this@CollectZoneContainerActivity
                                        )
                                    }

                                    override fun onLoaderReset(loader: Loader<Result<SirenContainerDto>>?) {
                                        Log.i(TAG, "onLoaderResetConfiguration")
                                        loader?.stopLoading()
                                    }
                                })
                            }
                            dialog.dismiss()
                        }
                    })
                }
                dialog.show()
            }
        }
    }
}