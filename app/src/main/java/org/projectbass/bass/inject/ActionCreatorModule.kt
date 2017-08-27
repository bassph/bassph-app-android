package org.projectbass.bass.inject

import org.projectbass.bass.flux.Dispatcher
import org.projectbass.bass.flux.Utils
import org.projectbass.bass.flux.action.DataCollectionActionCreator
import org.projectbass.bass.flux.action.LocationPointsActionCreator
import org.projectbass.bass.flux.model.DataCollectionModel
import org.projectbass.bass.flux.model.LocationPointsModel

import dagger.Module
import dagger.Provides

/**
 * @author A-Ar Andrew Concepcion
 */
@Module
internal class ActionCreatorModule {
    @Provides
    @PerApplication
    fun providesDataCollectionActionCreator(dispatcher: Dispatcher,
                                            model: DataCollectionModel,
                                            utils: Utils): DataCollectionActionCreator {
        return DataCollectionActionCreator(dispatcher, utils, model)
    }

    @Provides
    @PerApplication
    fun providesLocationPointsActionCreator(dispatcher: Dispatcher,
                                            model: LocationPointsModel,
                                            utils: Utils): LocationPointsActionCreator {
        return LocationPointsActionCreator(dispatcher, utils, model)
    }
}
