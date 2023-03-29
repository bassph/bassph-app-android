package org.projectbass.bass.ui.main

import android.Manifest.permission.*
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.Color
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.widget.*
import butterknife.BindView
import butterknife.OnCheckedChanged
import butterknife.OnClick
import cn.pedant.SweetAlert.SweetAlertDialog
import co.mobiwise.materialintro.shape.Focus
import co.mobiwise.materialintro.shape.FocusGravity
import co.mobiwise.materialintro.shape.ShapeType
import co.mobiwise.materialintro.view.MaterialIntroView
import com.cardiomood.android.controls.gauge.SpeedometerGauge
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.github.pwittchen.reactivewifi.AccessRequester
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.hsalf.smilerating.BaseRating
import com.hsalf.smilerating.SmileRating
import jonathanfinerty.once.Once
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.share_results.*
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

    private fun requestFineLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION)
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
            PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION,
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

        setupGauge()

        initFlux()

        isConnected.subscribe({
            SharedPrefUtil.retrieveTempData(this)?.let {
                it?.let {
                    postToServer(it)
                }
            }
        }, Firebase.crashlytics::recordException)
    }

    private fun setupGauge() {

        // Add label converter
        speedometer.labelConverter = SpeedometerGauge.LabelConverter { progress, maxProgress ->
            return@LabelConverter Math.round(progress).toInt().toString()
        }

        speedometer.labelTextSize = 40

        // configure value range and ticks
        speedometer.maxSpeed = 30.0
        speedometer.majorTickStep = 5.0
        speedometer.minorTicks = 0

        speedometer.addColoredRange(0.0, 5.0, Color.RED)
        speedometer.addColoredRange(5.0, 10.0, Color.parseColor("#ffa500"))
        speedometer.addColoredRange(10.0, 15.0, Color.YELLOW)
        speedometer.addColoredRange(15.0, 20.0, Color.parseColor("#90ee90"))
        speedometer.setSpeed(0.0, false)
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
                .subscribe { store ->
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
                }
        )
    }

    private fun getCarrierUserSatisfaction(data: Data) {
        SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE).apply {
            titleText = "How do you feel about your connection?"
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
        pDialog?.dismissWithAnimation()
        share_results.visibility = View.VISIBLE

        val data = dataCollectionStore.data!!

        if (data.connectivity != null) {
            if (data.connectivity.type == ConnectivityManager.TYPE_WIFI) {
                speed.text = "WiFi Speed: "
            } else {
                speed.text = "Data Speed: "
            }
        }

        if (!TextUtils.isEmpty(data.bandwidth)) {
            val bandwidth = data.bandwidth.replace(" Kbps", "").toFloat()
            val bandwidthString: String
            if (bandwidth < 1024) {
                bandwidthString = "$bandwidth Kbps"

                speedometer.maxSpeed = 30.0
                speedometer.majorTickStep = 5.0
                speedometer.minorTicks = 0

                speedometer.addColoredRange(20.0, speedometer.maxSpeed, Color.GREEN)

                speedometer.setSpeed(1.0, false)
            } else {
                bandwidthString = "${(Math.round(bandwidth / 1024))} Mbps"

                if (Math.round(bandwidth / 1024) > 30) {
                    speedometer.maxSpeed = Math.round(bandwidth / 1024).toDouble()
                    speedometer.majorTickStep = 10.0
                    speedometer.minorTicks = 0
                }

                speedometer.addColoredRange(20.0, speedometer.maxSpeed, Color.GREEN)

                speedometer.setSpeed((bandwidth / 1024).toDouble(), false)

            }

            speed.text = "${speed.text} $bandwidthString"
        }

        signal.text = "Signal: " + data.signal


        done.setOnClickListener {
            // TODO: Don't treat shared prefs as database
            SharedPrefUtil.clearTempData(this)
            resetView()
        }


        share.setOnClickListener {
            if (ShareDialog.canShow(SharePhotoContent::class.java)) {
                share_card.isDrawingCacheEnabled = true
                val bitmap = Bitmap.createBitmap(share_card.drawingCache)
                share_card.isDrawingCacheEnabled = false

                val linkContent = SharePhotoContent.Builder()
                        .setPhotos(listOf(SharePhoto.Builder().setBitmap(bitmap).setCaption("My BASS Results").build()))
                        .build()

                ShareDialog.show(this@MainActivity, linkContent)
            }

            SharedPrefUtil.clearTempData(this)
            resetView()
        }
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
        if (fineLocationPermissionNotGranted) {
            requestFineLocationPermission()
            firebaseAnalytics.logEvent("permission_denied", Bundle().apply { putString("permission", ACCESS_FINE_LOCATION) })
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
        FirebaseAnalytics.getInstance(this).logEvent("begin_test", Bundle())
        DataCollectionJob.scheduleJob(this)
    }

    fun endTest() {
        runOnUiThread { this.resetView() }
    }

    fun resetView() {
        reportText.visibility = View.VISIBLE
        share_results.visibility = View.GONE
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
        val PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1000
        private val PERMISSIONS_REQUEST_READ_PHONE_STATE = 1001
    }
}
