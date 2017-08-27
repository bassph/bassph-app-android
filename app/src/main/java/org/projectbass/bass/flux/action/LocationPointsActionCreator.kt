package org.projectbass.bass.flux.action

import android.support.annotation.StringDef
import org.projectbass.bass.flux.Action
import org.projectbass.bass.flux.Dispatcher
import org.projectbass.bass.flux.Utils
import org.projectbass.bass.flux.model.LocationPointsModel

/**
 * @author A-Ar Andrew Concepcion
 */
class LocationPointsActionCreator(private val mDispatcher: Dispatcher, private val mUtils: Utils, private val mModel: LocationPointsModel) {

    companion object {
        const val ACTION_GET_LOCATION_POINTS_S = "ACTION_GET_LOCATION_POINTS_S"
        const val ACTION_GET_LOCATION_POINTS_F = "ACTION_GET_LOCATION_POINTS_F"
    }

    @StringDef(value = *arrayOf(ACTION_GET_LOCATION_POINTS_S, ACTION_GET_LOCATION_POINTS_F))
    @Retention(AnnotationRetention.SOURCE)
    annotation class LocationPointsAction

    fun getLocationPoints() {
        mModel.getLocationPoints()
                .subscribe({ mDispatcher.dispatch(Action.create(ACTION_GET_LOCATION_POINTS_S, it)) }) { throwable ->
                    mDispatcher.dispatch(Action.create(ACTION_GET_LOCATION_POINTS_F, mUtils.getError(throwable))) }
    }
}
