package org.projectbass.bass.flux

/**
 * @author A-Ar Andrew Concepcion
 */
class AppError(val statusCode: Int, val errorCode: Int, val errorMessage: String, val network: Boolean?) {
    companion object {
        fun createNetwork(errorMessage: String): AppError {
            return AppError(-1, -1, errorMessage, true)
        }

        fun createHttp(errorMessage: String): AppError {
            return AppError(-1, -1, errorMessage, false)
        }

        fun createHttp(statusCode: Int, errorCode: Int, errorMessage: String): AppError {
            return AppError(statusCode, errorCode, errorMessage, false)
        }
    }
}