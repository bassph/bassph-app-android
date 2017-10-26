package org.projectbass.bass.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.ViewGroup
import android.widget.ImageView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.crashlytics.android.Crashlytics
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import org.projectbass.bass.R
import org.projectbass.bass.flux.action.LocationPointsActionCreator
import org.projectbass.bass.flux.store.LocationPointsStore
import org.projectbass.bass.ui.BaseActivity
import org.projectbass.bass.ui.main.MainActivity
import pl.charmas.android.reactivelocation.ReactiveLocationProvider
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject




class MapsActivity : BaseActivity(), OnMapReadyCallback {
    override val layoutRes: Int = R.layout.activity_maps
    private var mMap: GoogleMap? = null

    @Inject lateinit internal var locationPointsActionCreator: LocationPointsActionCreator
    @Inject lateinit internal var locationPointsStore: LocationPointsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent.inject(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.setOnCameraIdleListener(clusterManager)
        mMap!!.setOnMarkerClickListener(clusterManager)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(12.8894275, 122.2364049), 5.63f))

        loadLocationPoints()

        val fineLocationPermissionNotGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        val coarseLocationPermissionNotGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        if (fineLocationPermissionNotGranted && coarseLocationPermissionNotGranted) {
            requestCoarseLocationPermission()
            return
        }
        showPresentLocation()
    }

    private val clusterManager: ClusterManager<MyClusterItem> by lazy {
        return@lazy ClusterManager<MyClusterItem>(this, mMap!!)
    }

    private fun loadLocationPoints() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper?.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = "Loading"
        pDialog.contentText = "This might take a while for first load"
        pDialog.setCancelable(false)
        pDialog.show()
        clusterManager.renderer = MarkerRenderer(this, mMap!!, clusterManager)
        addSubscriptionToUnsubscribe(locationPointsStore.observable()
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { Observable.from(it.locationPoints) }
                .subscribe({
                    val locationPoint = MyClusterItem(lat = it.loc.lat, lng = it.loc.lng, locationPoint = it)
                    clusterManager.addItem(locationPoint)
//                    mMap!!.addMarker(MarkerOptions()
//                            .position(LatLng(it.loc.lat, it.loc.lng))
//                            .snippet("speed: ${it.bandwidth} \nsignal: ${it.signal}")
//                            .title(it.label))
                }, {
                    Crashlytics.logException(it)
                    pDialog.setTitleText("Error!")
                            .setConfirmText("Got It")
                            .setContentText("We're very sorry, something went wrong.\n" +
                                    "We're notified of this error and we'll fix this as soon as we can")
                            .setConfirmClickListener { dialog ->
                                dialog.dismissWithAnimation()
                            }
                            ?.changeAlertType(SweetAlertDialog.ERROR_TYPE)
                }, {
                    pDialog.dismiss()
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(12.8894275, 122.2364049), 5.8f))
                })
        )
        if (locationPointsStore.locationPoints.isNotEmpty()) {
            locationPointsStore.locationPoints.forEach {
                val locationPoint = MyClusterItem(lat = it.loc.lat, lng = it.loc.lng, locationPoint = it)
                clusterManager.addItem(locationPoint)
            }
            pDialog.dismiss()
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(12.8894275, 122.2364049), 5.8f))
        } else {
            locationPointsActionCreator.getLocationPoints()
        }

    }

    private fun requestCoarseLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    MainActivity.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION)
        }
    }

    fun showPresentLocation() {
        val request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1)
                .setInterval(100)
        val locationProvider = ReactiveLocationProvider(this)

        addSubscriptionToUnsubscribe(locationProvider.getUpdatedLocation(request).first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 10f))
                }, Crashlytics::logException))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MainActivity.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showPresentLocation()
                }
            }
        }
    }
}

private class MarkerRenderer(val context: Context, val map: GoogleMap, clusterManager: ClusterManager<MyClusterItem>) : DefaultClusterRenderer<MyClusterItem>(context, map, clusterManager) {
    private val mIconGenerator = IconGenerator(context)
    private val mImageView: ImageView = ImageView(context)
    private val mDimension: Int = context.resources.getDimension(R.dimen.marker_image).toInt()

    init {
        mImageView.layoutParams = ViewGroup.LayoutParams(mDimension, mDimension)
        val padding = context.resources.getDimension(R.dimen.marker_padding).toInt()
        mImageView.setPadding(padding, padding, padding, padding)
        mIconGenerator.setContentView(mImageView)
    }

    override fun onBeforeClusterItemRendered(item: MyClusterItem, markerOptions: MarkerOptions) {
        // Draw a single person.
        // Set the info window to show their name.
        val telco = item.title.toLowerCase()
        if (telco.contains("globe")) {
            mImageView.setImageResource(R.drawable.ic_globe)
        } else if (telco.contains("smart")) {
            mImageView.setImageResource(R.drawable.ic_smart)
        } else if (telco.contains("tnt")
                || (telco.contains("talk") and telco.contains("text"))) {
            mImageView.setImageResource(R.drawable.ic_tnt)
        } else if (telco.contains("sun")) {
            mImageView.setImageResource(R.drawable.ic_sun)
        } else if (telco.contains("tm")) {
            mImageView.setImageResource(R.drawable.ic_tm)
        } else if (telco.contains("wifi")) {
            mImageView.setImageResource(R.drawable.ic_wifi)
        } else {
            mImageView.setImageResource(R.drawable.ic_questionmark)
        }

        val icon = mIconGenerator.makeIcon()
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.title)
    }
}

