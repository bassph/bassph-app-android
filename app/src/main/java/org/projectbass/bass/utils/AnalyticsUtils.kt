package org.projectbass.bass.utils

import android.net.ConnectivityManager
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.projectbass.bass.model.Data

/**
 * @author A-Ar Andrew Concepcion
 * *
 * @createdOn 26/10/2017
 */
class AnalyticsUtils(val firebaseAnalytics: FirebaseAnalytics) {

    fun logPostData(data: Data) {
        firebaseAnalytics.logEvent ("data_sent", Bundle().apply {
            putString("Operator", data.operator)
            putString("Bandwidth", data.bandwidth)
            putString("ConnectionType", if (data.connectivity?.type == ConnectivityManager.TYPE_WIFI) "wifi" else "mobile data")
            putString("Signal", data.signal)
        })

        firebaseAnalytics.logEvent ("mood_satisfactory_rating", Bundle().apply {
            putInt("rating", data.mood ?: 0)
            putString("Bandwidth", data.bandwidth)
            putString("ConnectionType", if (data.connectivity?.type == ConnectivityManager.TYPE_WIFI) "wifi" else "mobile data")
            putString("Signal", data.signal)
        })

        val bundle = Bundle().apply {
            putString("Operator", data.operator)
            putString("Bandwidth", data.bandwidth)
            putString("Signal", data.signal)
            putString("ConnectionType", if (data.connectivity?.type == ConnectivityManager.TYPE_WIFI) "wifi" else "mobile data")
            putInt("Mood", data.mood ?: 0)
        }

        firebaseAnalytics.setUserId(data.imei)
        firebaseAnalytics.setUserProperty("Operator", data.operator)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle)
    }
}
