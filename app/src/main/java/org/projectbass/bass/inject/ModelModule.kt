package org.projectbass.bass.inject

import android.content.Context
import org.projectbass.bass.core.Database
import org.projectbass.bass.flux.model.DataCollectionModel
import org.projectbass.bass.flux.model.LocationPointsModel
import org.projectbass.bass.model.Sources
import org.projectbass.bass.post.api.RestAPI
import dagger.Module
import dagger.Provides

@Module
internal class ModelModule {

    @PerApplication
    @Provides
    fun providesDataCollectionModel(context: Context, restApi: RestAPI, sources: Sources, database: Database): DataCollectionModel {
        return DataCollectionModel(context, restApi, sources, database)
    }

    @PerApplication
    @Provides
    fun providesLocationPointsModel(restApi: RestAPI): LocationPointsModel {
        return LocationPointsModel(restApi)
    }
}
