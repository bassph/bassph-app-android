package org.projectbass.bass.flux.store

import org.projectbass.bass.flux.Action
import org.projectbass.bass.flux.AppError
import org.projectbass.bass.flux.Store
import org.projectbass.bass.flux.action.LocationPointsActionCreator
import org.projectbass.bass.model.LocationPoint
import rx.Observable

/**
 * @author A-Ar Andrew Concepcion
 */
class LocationPointsStore : Store<LocationPointsStore>() {
    var locationPoints: List<LocationPoint> = listOf()
        private set
    var action: String? = null
        private set
    var error: AppError? = null
        private set

    fun observableWithFilter(filter: String): Observable<LocationPointsStore> {
        return observable().filter { store -> filter == store.action }
    }

    private fun updateState() {
        error = null
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateData(action: Action) {
        if (action.data() is List<*>) {
            locationPoints = action.data() as List<LocationPoint>
        }
    }

    private fun updateError(action: Action) {
        if (action.data() != null && action.data() is AppError) {
            error = action.data() as AppError?
        }
    }

    private fun updateAction(action: Action) {
        this.action = action.type()
    }

    override fun onReceiveAction(action: Action) {
        updateAction(action)
        when (action.type()) {
            LocationPointsActionCreator.ACTION_GET_LOCATION_POINTS_S -> {
                updateState()
                updateData(action)
                notifyStoreChanged(this)
            }
            LocationPointsActionCreator.ACTION_GET_LOCATION_POINTS_F -> {
                updateError(action)
                notifyStoreChanged(this)
            }
        }
    }
}
