package ps.isel.ipl.pt.gabageapp

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.support.v7.app.AppCompatActivity
import android.app.LoaderManager.LoaderCallbacks
import android.content.Loader
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView

import android.util.Base64
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_login.*
import ps.isel.ipl.pt.gabageapp.service_web_api.service.HttpRequestService
import ps.isel.ipl.pt.gabageapp.service_web_api.WasteManageApi
import ps.isel.ipl.pt.gabageapp.util.loader.*
import ps.isel.ipl.pt.gabageapp.util.loader.json_home.HomeJson
import ps.isel.ipl.pt.gabageapp.util.loader.siren.dto.SirenUserDto

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(), LoaderCallbacksError<HomeJson> {

    private val LOADER_ID = 5847
    private val REQUEST = "request"
    private lateinit var home :HomeJson
    private val LOCATION_PERMS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
    private val PERMISSION_ACCESS_COARSE_LOCATION = 3695
    private val loginCallback = object : LoaderCallbacks<Result<SirenUserDto>>{
        override fun onCreateLoader(p0: Int, args: Bundle): Loader<Result<SirenUserDto>> {
            return AsyncTaskLoaderApi<SirenUserDto>(
                    SirenUserDto::class.java,
                    args.getParcelable(REQUEST),
                    this@LoginActivity
            )
        }

        override fun onLoadFinished(loader: Loader<Result<SirenUserDto>>?, result: Result<SirenUserDto>?) {
            showProgress(false)
            loader?.reset()
            if(result!=null){
                if(result.result!=null) {
                    val user = result.result.toUser()
                    user.action(this@LoginActivity, home)
                }
                else if(result.error!=null){
                    password.error = getString(R.string.error_incorrect_password)
                    password.requestFocus()
                }
            }
        }

        override fun onLoaderReset(loader: Loader<Result<SirenUserDto>>?) {
            loader?.stopLoading()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, LOCATION_PERMS,
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }

        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })
        var bundel = Bundle()
        var http = HttpLoader("${WasteManageApi.HOST_NAME_API}/", arrayOf(Header("Accept", "application/json-home")))
        bundel.putParcelable(REQUEST, http)
        loaderManager.initLoader<Result<HomeJson>>(LOADER_ID, bundel, this@LoginActivity)
    }

    override fun onSuccess(result: HomeJson) {
        home = result
        email_sign_in_button.setOnClickListener { attemptLogin() }
    }

    override fun onCreateLoader(p0: Int, args: Bundle): Loader<Result<HomeJson>> {
        return AsyncTaskLoaderApi<HomeJson>(
                HomeJson::class.java,
                args.getParcelable(REQUEST),
                this
        )
    }

    override fun onLoaderReset(loader: Loader<Result<HomeJson>>?) {
        loader?.reset()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_ACCESS_COARSE_LOCATION -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        loaderManager.destroyLoader(LOADER_ID)

        // Reset errors.
        email.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            var bundel = Bundle()
            val auth = Base64.encodeToString("$emailStr:$passwordStr".toByteArray(), 2)
            var http = HttpLoader("${WasteManageApi.HOST_NAME_API}${home.resources.getCurrentEmployee.href}", arrayOf(Header("Accept", "application/vnd.siren+json")))
            bundel.putParcelable(REQUEST, http)
            HttpRequestService.setAuth(auth)
            loaderManager.initLoader<Result<SirenUserDto>>(LOADER_ID, bundel, loginCallback)
            /*mAuthTask = UserLoginTask(emailStr, passwordStr, this)
            mAuthTask!!.execute(null as Void?)*/
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }
}
