package org.projectbass.bass.post.api

import org.projectbass.bass.model.Data
import org.projectbass.bass.model.LocationPoint
import org.projectbass.bass.model.RecordResponse

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import rx.Observable

/**
 * Paul Sydney Orozco (@xtrycatchx) on 4/2/17.
 */

interface RestAPI {

    @POST("scanresults")
    fun record(@Body data: Data): Observable<RecordResponse>

    @GET("locationpoints")
    fun getLocationPoints(): Observable<List<LocationPoint>>

    companion object {
        val BASE_URL = "https://bass.bnshosting.net/api/"
    }

}
