package org.projectbass.bass.service.job

import android.content.Context
import android.os.Bundle
import androidx.work.*
import com.google.firebase.analytics.FirebaseAnalytics
import org.projectbass.bass.flux.action.DataCollectionActionCreator
import org.projectbass.bass.flux.model.DataCollectionModel
import org.projectbass.bass.model.Data
import org.projectbass.bass.utils.SharedPrefUtil
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 09/07/2017
 */

class DataCollectionJob(
    val context: Context,
    workerParams: WorkerParameters,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val dataCollectionActionCreator: DataCollectionActionCreator,
    private val dataCollectionModel: DataCollectionModel
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!SharedPrefUtil.retrieveFlag(context, "auto_measure")) return Result.success()
        var data: Data?
        try {
            data = dataCollectionModel.executeNetworkTest().toBlocking().toFuture().get()
        } catch (e: InterruptedException) {
            firebaseAnalytics.logEvent("data_collection_failed", Bundle().apply { putBoolean("is_auto", true) })
            return Result.retry()
        } catch (e: ExecutionException) {
            firebaseAnalytics.logEvent("data_collection_failed", Bundle().apply { putBoolean("is_auto", true) })
            return Result.retry()
        }

        data?.let {
            val bundle = Bundle().apply {
                putString("Operator", it.operator)
                putString("Bandwidth", it.bandwidth)
                putString("Signal", it.signal)
            }

            firebaseAnalytics.setUserId(it.imei)
            firebaseAnalytics.logEvent("auto_measure", bundle)
            dataCollectionActionCreator.sendData(it)
        }

        return Result.success()
    }

    companion object {
        const val TAG = "DataCollectionWorker"

        fun scheduleJob(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<DataCollectionJob>(30, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag(TAG)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.UPDATE, request)
        }
    }
}