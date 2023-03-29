package org.projectbass.bass.flux

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import retrofit2.adapter.rxjava.HttpException

/**
 * @author A-Ar Andrew Concepcion
 */
class Utils {

    fun getError(throwable: Throwable): AppError {
        Firebase.crashlytics.recordException(throwable)
        if (throwable !is HttpException) {
            return AppError.createNetwork(MSG_ERROR_DEFAULT)
        }
        val response = throwable.response() ?: return AppError.createHttp(MSG_ERROR_DEFAULT)
        return AppError.createHttp(response.code(), -1, MSG_ERROR_DEFAULT)
    }

    companion object {
        val MSG_ERROR_DEFAULT = "Something went wrong."
        val MSG_ERROR_NETWORK = "No network connection."
    }
}
