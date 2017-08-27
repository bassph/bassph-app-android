package org.projectbass.bass.model

/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 11/08/2017
 */
data class Location(
        val mAccuracy: Double,
        val mAltitude: Double,
        val mBearing: Double,
        val mElapsedRealtimeNanos: Long,
        val mLatitude: Double,
        val mLongitude: Double,
        val mSpeed: Double,
        val mTime: Long,
        val mProvider: String
)