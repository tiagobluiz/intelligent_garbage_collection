package ps.isel.ipl.pt.gabageapp.manage.fragments

import android.annotation.SuppressLint
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.Loader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_profile.*
import ps.isel.ipl.pt.gabageapp.R
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.json_home.Resources
import ps.isel.ipl.pt.gabageapp.util.loader.siren.dto.SirenUserDto
import ps.isel.ipl.pt.gabageapp.util.loader.v4.AsyncTaskLoaderApiV4
import ps.isel.ipl.pt.gabageapp.util.loader.v4.LoaderCallbacksErrorV4


@SuppressLint("ValidFragment")
class ProfileFragment(val resources: Resources) : Fragment(), LoaderCallbacksErrorV4<SirenUserDto> {

    private val REQUEST = "request"
    private val LOADER_ID = 36841

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var bundel = Bundle()
        var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${resources.getCurrentEmployee.href}", arrayOf(Header("Accept","application/vnd.siren+json")))
        bundel.putParcelable(REQUEST,http)
        loaderManager.initLoader<Result<SirenUserDto>>(LOADER_ID, bundel, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Result<SirenUserDto>> {
        return AsyncTaskLoaderApiV4<SirenUserDto>(
                SirenUserDto::class.java,
                args.getParcelable(REQUEST),
                context!!
        )
    }

    override fun onLoaderReset(loader: Loader<Result<SirenUserDto>>?) {
        loader?.reset()
    }

    override fun onSuccess(result: SirenUserDto) {
        val user = result.toUser()
        office_text.text = user.post
        username_text.text = user.username
        email_text.text = user.email
        phone_text.text = "${user.phoneNumber}"
    }
}
