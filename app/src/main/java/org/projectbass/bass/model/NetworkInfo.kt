package org.projectbass.bass.model

/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 11/08/2017
 */

data class NetworkInfo(
        val cid: Int,
        val lac: Int,
        val mcc: Int,
        val mnc: Int
)