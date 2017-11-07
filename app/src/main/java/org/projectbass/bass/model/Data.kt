package org.projectbass.bass.model

import android.content.Context
import android.net.ConnectivityManager
import android.text.TextUtils
import org.projectbass.bass.R


/**
 * Paul Sydney Orozco (@xtrycatchx) on 4/2/17.
 */

data class Data(val connectivity: Connectivity? = null,
                val location: Location? = null,
                val operator: String,
                var mood: Int? = null,
                val device: Device,
                val imei: String,
                val signal: String,
                val networkInfo: NetworkInfo? = null,
                val bandwidth: String = "0",
                val version: String) {

    fun toString(context: Context): String {
        val sb = StringBuilder()
//        if (!TextUtils.isEmpty(operator)) {
//            sb.append(context.getString(R.string.provider))
//            sb.append(operator)
//            sb.append("\n")
//        }

        if (connectivity != null) {
            sb.append(context.getString(R.string.connection_type))
            if (connectivity.type == ConnectivityManager.TYPE_WIFI) {
                sb.append("WiFi")
            } else {
                sb.append("Mobile Data")
            }
            sb.append("\n")
        }

        if (!TextUtils.isEmpty(bandwidth)) {
            sb.append(context.getString(R.string.bandwidth))
            val bandwidth = bandwidth.replace(" Kbps", "").toFloat()
            val bandwidthString: String
            if (bandwidth < 1024) {
                bandwidthString = "$bandwidth Kbps"
            } else {
                bandwidthString = "${(Math.round(bandwidth / 1024 * 100.0) / 100.0)} Mbps"
            }
            sb.append(bandwidthString)
            sb.append("\n")
        }
        if (!TextUtils.isEmpty(signal)) {
            sb.append(context.getString(R.string.signal))
            sb.append(signal)
        }
        return sb.toString()
    }
}