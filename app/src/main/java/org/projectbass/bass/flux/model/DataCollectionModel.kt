package org.projectbass.bass.flux.model

import android.content.Context
import android.net.ConnectivityManager
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork
import com.google.android.gms.location.LocationRequest
import org.projectbass.bass.core.Database
import org.projectbass.bass.model.*
import org.projectbass.bass.post.api.RestAPI
import pl.charmas.android.reactivelocation.ReactiveLocationProvider
import rx.Observable
import rx.schedulers.Schedulers

/**
 * @author A-Ar Andrew Concepcion
 */
class DataCollectionModel(private val mContext: Context, private val mRestApi: RestAPI, private val mSources: Sources, private val database: Database) {
    @Throws(SecurityException::class)
    fun executeNetworkTest(): Observable<Data> {
        val connectivityObservable = ReactiveNetwork.observeNetworkConnectivity(mContext).first().map {
            return@map Connectivity(
                    available = it.isAvailable,
                    detailedState = it.detailedState.name,
                    extraInfo = it.extraInfo,
                    failover = it.isFailover,
                    roaming = it.isRoaming,
                    state = it.state.name,
                    subType = it.subType,
                    subTypeName = it.subTypeName,
                    type = it.type,
                    typeName = it.typeName
            )
        }
        val request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1)
                .setInterval(100)
        val locationProvider = ReactiveLocationProvider(mContext)

        val networkOperator = mSources.networkOperator()
        val device = mSources.device()
        val IMEI = mSources.imei()
        val signal = mSources.signal()
        val version = mSources.version()
        val networkInfo = mSources.getNetworkInfo()
        var currentData = Data(
                operator = networkOperator,
                device = device,
                imei = IMEI,
                signal = signal,
                networkInfo = networkInfo,
                version = version)

        val locationObservable = locationProvider.getUpdatedLocation(request).first().map {
            return@map Location(
                    mAltitude = it.altitude,
                    mAccuracy = it.accuracy.toDouble(),
                    mBearing = it.bearing.toDouble(),
                    mElapsedRealtimeNanos = it.elapsedRealtimeNanos,
                    mLongitude = it.longitude,
                    mLatitude = it.latitude,
                    mTime = it.time,
                    mSpeed = it.speed.toDouble(),
                    mProvider = it.provider)
        }
        val bandwidthObservable = mSources.bandwidth().subscribeOn(Schedulers.io())
        return bandwidthObservable
                .doOnNext { currentData = currentData.copy(bandwidth = it)}
                .flatMap { connectivityObservable }
                .doOnNext { currentData = currentData.copy(connectivity = it) }
                .flatMap { locationObservable }
                .map { currentData.copy(location = it) }
    }

    fun sendData(data: Data): Observable<RecordResponse> {

        return mRestApi.record(data).doOnNext {
            database.store().upsert(
                    History(testId = it.id,
                            operator = data.operator,
                            signal = data.signal,
                            bandwidth = data.bandwidth,
                            connectionType = if (data.connectivity?.type == ConnectivityManager.TYPE_WIFI) "wifi" else "mobile data",
                            createdDate = System.currentTimeMillis()))
                    .subscribe()
        }
    }
}
