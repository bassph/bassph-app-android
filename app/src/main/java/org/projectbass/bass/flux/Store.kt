package org.projectbass.bass.flux

import android.support.annotation.UiThread

import rx.Observable
import rx.subjects.PublishSubject

/**
 * This class reacts to actions dispatched by the [Dispatcher] and is
 * responsible for storing and updating its own state.

 * @author A-Ar Andrew Concepcion
 */
abstract class Store<Store> {

    private val mPublisher = PublishSubject.create<Store>()

    /**
     * Returns the [Observable] to subscribe, by default this will run
     * synchronously on the UI Thread so we must not call
     * [Observable.subscribeOn] or [Observable.observeOn]
     * unless thread safety is not needed.
     */
    fun observable(): Observable<Store> {
        return mPublisher.asObservable()
    }

    /**
     * Dispatch an action to the store, this method must be called on the UI Thread.
     */
    @UiThread
    fun dispatchAction(action: Action) {
        onReceiveAction(action)
    }

    /**
     * Called when the [Dispatcher] dispatches an action.
     * This methods run synchronously on the UI Thread so we must not do
     * heavy computations so that we dont stall the UI.
     */
    protected abstract fun onReceiveAction(action: Action)

    /**
     * Notify the subscribers that the store changes.
     */
    protected fun notifyStoreChanged(t: Store) {
        mPublisher.onNext(t)
    }
}
