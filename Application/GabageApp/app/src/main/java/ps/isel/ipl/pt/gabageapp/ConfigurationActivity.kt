package ps.isel.ipl.pt.gabageapp

import android.content.Loader
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_configuration.*
import kotlinx.android.synthetic.main.content_configuration.*
import kotlinx.android.synthetic.main.new_communication_dialog.view.*
import ps.isel.ipl.pt.gabageapp.model.CommunicationDetails
import ps.isel.ipl.pt.gabageapp.service.ServiceLocator
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi
import ps.isel.ipl.pt.gabageapp.service_web_api.service.ResulFromServiceErro
import ps.isel.ipl.pt.gabageapp.util.array_adapters.ArrayAdapterCommunications
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.collection.CollectionJson
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Action
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Field
import ps.isel.ipl.pt.gabageapp.util.loader.siren.Option
import ps.isel.ipl.pt.gabageapp.util.loader.siren.dto.SirenCommunicationDto
import ps.isel.ipl.pt.gabageapp.util.loader.siren.dto.SirenConfigurationDto
import ps.isel.ipl.pt.gabageapp.util.scroll.EndlessScrollListener

class ConfigurationActivity : AppCompatActivity(), LoaderCallbacksError<CollectionJson> {

    companion object {
        val GET_CONFIGURATIONS = "getConfigurations"
    }

    private val REQUEST = "request"
    private val TAG = "ConfigurationActivity"
    private val LOADER_ID = 5148
    private var LOADER_COMMUNICATION_ID = 5496
    private val SAVE_LIST_STATE = "communication_list_state"
    private lateinit var scrollListener: EndlessScrollListener
    private var communicationDetailsList: ArrayList<CommunicationDetails> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)
        setSupportActionBar(toolbar)

        val url = intent.extras.getString(GET_CONFIGURATIONS)

        var saved = savedInstanceState?.getParcelableArrayList<CommunicationDetails>(SAVE_LIST_STATE)
        if(saved != null) {
            Log.i(TAG, "Reload list saved")
            communicationDetailsList.addAll(0,saved)
        }

        var bundel = Bundle()
        var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${url}", arrayOf(Header("Accept","application/vnd.siren+json")))
        bundel.putParcelable(REQUEST,http)
        loaderManager.initLoader<Result<SirenConfigurationDto>>(LOADER_ID, bundel, object: LoaderCallbacksError<SirenConfigurationDto>{
            override fun onSuccess(result: SirenConfigurationDto) {
                val configuration = result.toConfiguration()
                configuration_id_text.text = "${configuration.configurationId}"
                configuartion_name_text.text = "${configuration.configurationName}"

                if(saved==null) {
                    var bundel = Bundle()
                    var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${configuration.communicationsUrl}", arrayOf(Header("Accept", "application/vnd.collection+json")))
                    bundel.putParcelable(REQUEST, http)
                    loaderManager.restartLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@ConfigurationActivity)
                }
            }

            override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<SirenConfigurationDto>> {
                Log.i(TAG, "onCreateLoaderConfiguration")
                return AsyncTaskLoaderApi<SirenConfigurationDto>(
                        SirenConfigurationDto::class.java,
                        args.getParcelable(REQUEST),
                        this@ConfigurationActivity
                )
            }

            override fun onLoaderReset(loader: Loader<Result<SirenConfigurationDto>>?) {
                Log.i(TAG, "onLoaderResetConfiguration")
                loader?.stopLoading()
            }
        })

        scrollListener = object : EndlessScrollListener(20) {
            override fun onLoadMore(link: String): Boolean {
                Log.i(TAG, "New Link is load")
                var bundel = Bundle()
                var http = HttpLoader("${WasteManageApi.HOST_NAME_API}$link", arrayOf(Header("Accept","application/vnd.collection+json")))
                bundel.putParcelable(REQUEST,http)
                loaderManager.restartLoader<Result<CollectionJson>>(LOADER_ID, bundel, this@ConfigurationActivity)
                return true
            }
        }

        configurations_list.adapter = ArrayAdapterCommunications(this@ConfigurationActivity, R.layout.communication_item, communicationDetailsList)
        (configurations_list.adapter as ArrayAdapterCommunications).notifyDataSetChanged()
        configurations_list.setOnScrollListener(scrollListener)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        Log.i(TAG, "onSaveInstanceState")
        outState?.putParcelableArrayList(SAVE_LIST_STATE, communicationDetailsList)
        super.onSaveInstanceState(outState)
    }

    override fun onSuccess(result: CollectionJson) {
        var next = result.collection.links.find { it.rel.equals("next") }?.href
        if (next == null)
            next = ""

        val communications = result.toCommunications()

        communications.forEach {
            var bundel = Bundle()
            var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${it.self}", arrayOf(Header("Accept","application/vnd.siren+json")))
            bundel.putParcelable(REQUEST,http)
            loaderManager.initLoader<Result<SirenCommunicationDto>>(LOADER_COMMUNICATION_ID++, bundel, object: LoaderCallbacksError<SirenCommunicationDto>{
                override fun onSuccess(result: SirenCommunicationDto) {
                    it.delete = object : View.OnClickListener{
                        override fun onClick(v: View?) {
                            var deleteAction = result.actions.find { it.name.equals("disassociate-communication-configuration")}
                            if(deleteAction !=null)
                                ServiceLocator.getMakeRequest().startAction(this@ConfigurationActivity, deleteAction, object : ResulFromServiceErro(this@ConfigurationActivity) {
                                    override fun onSuccess(redirect: String?) {
                                        communicationDetailsList.remove(it)
                                        (configurations_list.adapter as ArrayAdapterCommunications).notifyDataSetChanged()
                                        Toast.makeText(this@ConfigurationActivity, "Deleted", Toast.LENGTH_LONG).show()
                                    }
                                })
                        }
                    }
                    (configurations_list.adapter as ArrayAdapterCommunications).notifyDataSetChanged()
                }

                override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<SirenCommunicationDto>> {
                    Log.i(TAG, "onCreateLoaderConfiguration")
                    return AsyncTaskLoaderApi<SirenCommunicationDto>(
                            SirenCommunicationDto::class.java,
                            args.getParcelable(REQUEST),
                            this@ConfigurationActivity
                    )
                }

                override fun onLoaderReset(loader: Loader<Result<SirenCommunicationDto>>?) {
                    Log.i(TAG, "onLoaderResetConfiguration")
                    loader?.stopLoading()
                }
            })
        }

        add_communications_button.setOnClickListener {
            val communicationId = Field("communicationId","","","", arrayOf<Option>())
            val value = Field("value","","","", arrayOf<Option>())
            val fields = arrayListOf<Field>(communicationId, value)
            val newCommunication = Action("","","POST",result.collection.href, "application/json", fields)
            val config = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.new_communication_dialog, null)
            config.setView(view)
            val dialog = config.create()
            view.save_new_communication.setOnClickListener {
                communicationId.value = view.communication_id_new.text.toString()
                value.value = view.communication_value_new.text.toString()
                ServiceLocator.getMakeRequest().startAction(this@ConfigurationActivity, newCommunication, object : ResulFromServiceErro(this) {
                    override fun onSuccess(redirect: String?) {
                        dialog.dismiss()
                        var bundel = Bundle()
                        var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${redirect}", arrayOf(Header("Accept","application/vnd.siren+json")))
                        bundel.putParcelable(REQUEST,http)
                        loaderManager.initLoader<Result<SirenCommunicationDto>>(LOADER_COMMUNICATION_ID++, bundel, object: LoaderCallbacksError<SirenCommunicationDto>{
                            override fun onSuccess(result: SirenCommunicationDto) {
                                val communicationResult = result.toCommunication()
                                communicationResult.delete = object : View.OnClickListener{
                                    override fun onClick(v: View?) {
                                        var deleteAction = result.actions.find { it.name.equals("disassociate-communication-configuration")}
                                        if(deleteAction !=null)
                                            ServiceLocator.getMakeRequest().startAction(this@ConfigurationActivity, deleteAction, object : ResulFromServiceErro(this@ConfigurationActivity) {
                                                override fun onSuccess(redirect: String?) {
                                                    communicationDetailsList.remove(communicationResult)
                                                    (configurations_list.adapter as ArrayAdapterCommunications).notifyDataSetChanged()
                                                    Toast.makeText(this@ConfigurationActivity, "Deleted", Toast.LENGTH_LONG).show()
                                                }
                                            })
                                    }
                                }
                                communicationDetailsList.add(communicationResult)
                                (configurations_list.adapter as ArrayAdapterCommunications).notifyDataSetChanged()
                            }

                            override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<SirenCommunicationDto>> {
                                Log.i(TAG, "onCreateLoaderConfiguration")
                                return AsyncTaskLoaderApi<SirenCommunicationDto>(
                                        SirenCommunicationDto::class.java,
                                        args.getParcelable(REQUEST),
                                        this@ConfigurationActivity
                                )
                            }

                            override fun onLoaderReset(loader: Loader<Result<SirenCommunicationDto>>?) {
                                Log.i(TAG, "onLoaderResetConfiguration")
                                loader?.stopLoading()
                            }
                        })
                        dialog.show()
                    }
                })
            }
            dialog.show()
        }

        scrollListener.setNewLink(next)
        communicationDetailsList.addAll(communications)
        (configurations_list.adapter as ArrayAdapterCommunications).notifyDataSetChanged()
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<CollectionJson>> {
        Log.i(TAG, "onCreateLoaderCommunications")
        return AsyncTaskLoaderApi<CollectionJson>(
                CollectionJson::class.java,
                args.getParcelable(REQUEST),
                this@ConfigurationActivity
        )
    }

    override fun onLoaderReset(loader: Loader<Result<CollectionJson>>?) {
        Log.i(TAG, "onLoaderResetCommunication")
        loader?.stopLoading()
    }
}
