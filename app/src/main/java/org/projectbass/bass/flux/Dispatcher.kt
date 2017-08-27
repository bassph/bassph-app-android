package org.projectbass.bass.flux

import android.os.Handler
import android.os.Looper
import timber.log.Timber
import java.util.*

/**
 * Class responsible for dispatching actions throughout the application to the stores.

 * @author A-Ar Andrew Concepcion
 */
class Dispatcher(stores: List<Store<*>>) {

    private val mStores: List<Store<*>> = Collections.unmodifiableList(stores)
    private val mMainThreadHandler = Handler(Looper.getMainLooper())

    /**
     * Dispatch actions to the stores in UI Thread
     */
    fun dispatch(action: Action) {
        mMainThreadHandler.post {
            Timber.d("#dispatch action : %s", action)
            mStores.forEach {
                it.dispatchAction(action)
            }
        }
    }
}
