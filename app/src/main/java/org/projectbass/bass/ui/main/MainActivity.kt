package org.projectbass.bass.ui.main

import android.Manifest.permission.*
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import butterknife.BindView
import butterknife.OnCheckedChanged
import butterknife.OnClick
import cn.pedant.SweetAlert.SweetAlertDialog
import co.mobiwise.materialintro.shape.Focus
import co.mobiwise.materialintro.shape.FocusGravity
import co.mobiwise.materialintro.shape.ShapeType
import co.mobiwise.materialintro.view.MaterialIntroView
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import com.github.pwittchen.reactivewifi.AccessRequester
import com.google.firebase.analytics.FirebaseAnalytics
import com.hsalf.smilerating.BaseRating
import com.hsalf.smilerating.SmileRating
import jonathanfinerty.once.Once
import kotlinx.android.synthetic.main.activity_main.*
import org.projectbass.bass.R
import org.projectbass.bass.flux.action.DataCollectionActionCreator
import org.projectbass.bass.flux.store.DataCollectionStore
import org.projectbass.bass.model.Data
import org.projectbass.bass.post.api.RestAPI
import org.projectbass.bass.service.job.DataCollectionJob
import org.projectbass.bass.ui.BaseActivity
import org.projectbass.bass.ui.history.HistoryActivity
import org.projectbass.bass.ui.map.MapsActivity
import org.projectbass.bass.utils.AnalyticsUtils
import org.projectbass.bass.utils.SharedPrefUtil
import rx.Observable
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.net.InetAddress
import java.net.URI
import java.util.*
import javax.inject.Inject

/**
 * Paul Sydney Orozco (@xtrycatchx) on 4/2/17.
 */
class MainActivity : BaseActivity() {
    override val layoutRes: Int = R.layout.activity_main

    @BindView(R.id.centerImage) lateinit var centerImage: ImageView
    @BindView(R.id.btnMap) lateinit var map: Button
    @BindView(R.id.enableAutoMeasure) lateinit var enableAutoMeasure: CheckBox

    @Inject lateinit internal var dataCollectionActionCreator: DataCollectionActionCreator
    @Inject lateinit internal var dataCollectionStore: DataCollectionStore
    @Inject lateinit internal var analyticsUtils: AnalyticsUtils
    @Inject lateinit internal var firebaseAnalytics: FirebaseAnalytics

    private var pDialog: SweetAlertDialog? = null
    private var isAlreadyRunningTest: Boolean = false

    private fun requestCoarseLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(ACCESS_COARSE_LOCATION),
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION)
        }
    }

    private fun requestPhoneStatePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(READ_PHONE_STATE),
                    PERMISSIONS_REQUEST_READ_PHONE_STATE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION,
            PERMISSIONS_REQUEST_READ_PHONE_STATE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onCenterImageClicked()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent.inject(this)

        initUi()

        showTutorial()

        initFlux()

        isConnected.subscribe({
            SharedPrefUtil.retrieveTempData(this)?.let {
                it?.let {
                    postToServer(it)
                }
            }
        }, Crashlytics::logException)
    }

    private fun initUi() {
        enableAutoMeasure.isChecked = SharedPrefUtil.retrieveFlag(this, "auto_measure")
    }

    private fun showTutorial() {
        if (!Once.beenDone(Once.THIS_APP_INSTALL, "tutorial_measure")) {
            showMeasureTutorial()
        } else if (!Once.beenDone(Once.THIS_APP_SESSION, "tutorial_auto_measure") and
                !SharedPrefUtil.retrieveFlag(this, "auto_measure")) {
            showAutoMeasureTutorial()
        } else if (!Once.beenDone(Once.THIS_APP_INSTALL, "tutorial_map")) {
            showMapTutorial()
        }
    }

    private fun showMapTutorial() {
        showTutorialOverlay(message = "To view measurement reports, click Map.",
                target = map,
                focusType = Focus.ALL,
                listener = {
                    Once.markDone("tutorial_map")
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE,
                            Bundle().apply { putBoolean("is_complete", true) })
                })
    }

    private fun showMeasureTutorial() {
        showTutorialOverlay(message = "Hi There! To contribute to our data and see your network speed, please click the button in the center.",
                target = centerImage,
                focusType = Focus.ALL,
                listener = {
                    Once.markDone("tutorial_measure")
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN,
                            Bundle().apply { putBoolean("is_complete", false) })
                    showTutorial()
                })
    }

    private fun showAutoMeasureTutorial() {
        showTutorialOverlay(message = "If you want to send us your measurements regularly, please check this box. Let's check this for now to help us gather more data (You can uncheck it).",
                target = enableAutoMeasure,
                focusType = Focus.ALL,
                listener = {
                    enableAutoMeasure.isChecked = true
                    Once.markDone("tutorial_auto_measure")
                    showTutorial()
                })
    }

    fun showTutorialOverlay(target: View, message: String, focusType: Focus = Focus.NORMAL, listener: () -> Unit) {
        MaterialIntroView.Builder(this)
                .enableDotAnimation(false)
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(focusType)
                .dismissOnTouch(true)
                .setTargetPadding(30)
                .setDelayMillis(100)
                .enableFadeAnimation(true)
                .performClick(false)
                .setIdempotent(false)
                .setInfoText(message)
                .setShape(ShapeType.CIRCLE)
                .setTarget(target)
                .setUsageId(UUID.randomUUID().toString())
                .setListener { listener() }
                .show()
    }

    private fun initFlux() {
        addSubscriptionToUnsubscribe(dataCollectionStore.observable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ store ->
                    when (store.action) {
                        DataCollectionActionCreator.ACTION_COLLECT_DATA_S -> {
                            resetView()
                            store.data?.let {
                                getCarrierUserSatisfaction(it)
                            }

                        }
                        DataCollectionActionCreator.ACTION_SEND_DATA_S -> showShareResultDialog()
                        DataCollectionActionCreator.ACTION_SEND_DATA_F -> {
                            pDialog?.dismiss()
                            showErrorDialog()
                        }
                        DataCollectionActionCreator.ACTION_COLLECT_DATA_F -> {
                            pDialog?.dismiss()
                            firebaseAnalytics.logEvent("data_collection_failed", Bundle().apply { putBoolean("is_auto", false) })
                            resetView()
                        }
                    }
                }) { resetView() }
        )
    }

    private fun getCarrierUserSatisfaction(data: Data) {
        SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE).apply {
            titleText = "How do you feel about ${data.operator}?"
            confirmText = "Submit"
            setCancelable(false)
            val smileRatingView = initSmileRatingView()
            setConfirmClickListener { dialog ->
                data.mood = smileRatingView.rating - 3
                dismissWithAnimation()
                postToServer(data)
            }
            setCustomView(smileRatingView)
        }.show()
    }

    fun initSmileRatingView(): SmileRating {
        return SmileRating(this).apply {
            selectedSmile = BaseRating.OKAY
            setNameForSmile(BaseRating.TERRIBLE, "Angry")
            setNameForSmile(BaseRating.OKAY, "Meh")
        }
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(this)
                .setTitle("Error : ${dataCollectionStore.error?.statusCode}")
                .setMessage(dataCollectionStore.error?.errorMessage)
                .show()
    }

    private fun showShareResultDialog() {
        val result = dataCollectionStore.data?.toString(this@MainActivity)
        pDialog?.run {
            titleText = "Sent! Here's your data"
            cancelText = "I'm Done"
            setCancelClickListener({ it.dismissWithAnimation() })
            confirmText = "Share Results"
            contentText = result
            setConfirmClickListener { dialog ->
                if (ShareDialog.canShow(ShareLinkContent::class.java)) {
                    val linkContent = ShareLinkContent.Builder()
                            .setContentTitle("My BASS Results")
                            .setImageUrl(Uri.parse("https://scontent.fmnl4-6.fna.fbcdn.net/v/t1.0-9/17796714_184477785394716_1700205285852495439_n.png?oh=40acf149ffe8dcc0e24e60af7f844514&oe=595D6465"))
                            .setContentDescription(result)
                            .setContentUrl(Uri.parse("https://bass.bnshosting.net/device"))
                            .setShareHashtag(ShareHashtag.Builder()
                                    .setHashtag("#BASSparaSaBayan")
                                    .build())
                            .build()

                    ShareDialog.show(this@MainActivity, linkContent)
                }
            }
            changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
        }
        // TODO: Don't treat shared prefs as database
        SharedPrefUtil.clearTempData(this)
        resetView()
    }

    @OnClick(R.id.btnMap)
    fun onButtonMapsClicked() {
        val intent = Intent(this@MainActivity, MapsActivity::class.java)
        startActivity(intent)
    }

    @OnClick(R.id.btnHistory)
    fun onHistoryClicked() {
        val intent = Intent(this@MainActivity, HistoryActivity::class.java)
        startActivity(intent)
    }

    @OnClick(R.id.centerImage)
    fun onCenterImageClicked() {
        if (rippleBackground.isRippleAnimationRunning) {
            endTest()
        } else {
            reportText.visibility = View.INVISIBLE
            centerImage.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.signal_on))
            rippleBackground.startRippleAnimation()
            if (!isAlreadyRunningTest) {
                isAlreadyRunningTest = true
                runOnUiThreadIfAlive(Runnable { this.beginTest() }, 1000)
            }
        }
    }

    @OnCheckedChanged(R.id.enableAutoMeasure)
    fun onEnabledMeasure(v: CompoundButton, isChecked: Boolean) {
        firebaseAnalytics.logEvent("auto_measure", Bundle().apply { putBoolean("is_auto", isChecked) })
        SharedPrefUtil.saveFlag(this, "auto_measure", isChecked)
    }

    fun beginTest() {
        firebaseAnalytics.logEvent("begin_test", Bundle().apply { putBoolean("start", true) })
        val fineLocationPermissionNotGranted = ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
        val coarseLocationPermissionNotGranted = ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
        val phoneStatePermissionNotGranted = ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) != PERMISSION_GRANTED
        if (fineLocationPermissionNotGranted && coarseLocationPermissionNotGranted) {
            if (fineLocationPermissionNotGranted) {
                requestCoarseLocationPermission()
                firebaseAnalytics.logEvent("permission_denied", Bundle().apply { putString("permission", ACCESS_FINE_LOCATION) })
            }
            if (coarseLocationPermissionNotGranted) {
                firebaseAnalytics.logEvent("permission_denied", Bundle().apply { putString("permission", ACCESS_COARSE_LOCATION) })
            }
            isAlreadyRunningTest = false
            endTest()

            return
        }
        if (phoneStatePermissionNotGranted) {
            isAlreadyRunningTest = false
            requestPhoneStatePermission()
            endTest()
            firebaseAnalytics.logEvent("permission_denied", Bundle().apply { putString("permission", READ_PHONE_STATE) })
            return
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            val provider = Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
            if (!provider.contains(LocationManager.GPS_PROVIDER)) {
                runOnUiThread { AccessRequester.requestLocationAccess(this) }
                isAlreadyRunningTest = false
                endTest()
                return
            }
        } else {
            if (!AccessRequester.isLocationEnabled(this)) {
                runOnUiThread { AccessRequester.requestLocationAccess(this) }
                isAlreadyRunningTest = false
                endTest()
                return
            }
        }
        dataCollectionActionCreator.collectData()
        isAlreadyRunningTest = false
        Answers.getInstance().logCustom(CustomEvent("Begin Test"))
        DataCollectionJob.scheduleJob()
    }

    fun endTest() {
        runOnUiThread { this.resetView() }
    }

    fun resetView() {
        reportText.visibility = View.VISIBLE
        rippleBackground.stopRippleAnimation()
        centerImage.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.signal))
    }

    fun postToServer(data: Data) {

        analyticsUtils.logPostData(data)

        data.let {
            SharedPrefUtil.saveTempData(this, it)
            pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).apply {
                progressHelper?.barColor = Color.parseColor("#A5DC86")
                titleText = "Loading"
                setCancelable(false)
                show()
            }
            dataCollectionActionCreator.sendData(it)
        }
    }

    // TODO: Can be improved
    val isConnected: Single<Boolean>
        get() = Observable.fromCallable { InetAddress.getByName(URI.create(RestAPI.BASE_URL).host) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { inetAddress -> inetAddress != null }
                .toSingle()

    override fun onDestroy() {
        super.onDestroy()
        if (pDialog != null && pDialog!!.isShowing) {
            pDialog!!.dismiss()
        }
    }

    companion object {
        val PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1000
        private val PERMISSIONS_REQUEST_READ_PHONE_STATE = 1001
    }
}
