package org.projectbass.bass.flux.model

import org.projectbass.bass.model.LocationPoint
import org.projectbass.bass.post.api.RestAPI
import rx.Observable

/**
 * @author A-Ar Andrew Concepcion
 */
class LocationPointsModel(private val mRestApi: RestAPI) {
    fun getLocationPoints(): Observable<List<LocationPoint>> {
        return mRestApi.getLocationPoints()
    }
}
