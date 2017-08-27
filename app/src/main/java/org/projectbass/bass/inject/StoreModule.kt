package org.projectbass.bass.inject

import org.projectbass.bass.flux.Dispatcher
import org.projectbass.bass.flux.store.DataCollectionStore
import org.projectbass.bass.flux.store.LocationPointsStore
import dagger.Module
import dagger.Provides
import java.util.*

/**
 * Dagger module to provide flux components. This class will contain the Dispatcher,
 * ActionCreators, and Stores.

 * @author Gian Darren Aquino
 */
@Module
internal class StoreModule {

    @PerApplication
    @Provides
    fun providesDispatcher(dataCollectionStore: DataCollectionStore, locationPointsStore: LocationPointsStore): Dispatcher {
        return Dispatcher(Arrays.asList(
                dataCollectionStore,
                locationPointsStore))
    }

    @PerApplication
    @Provides
    fun providesDataCollectionStore(): DataCollectionStore {
        return DataCollectionStore()
    }

    @PerApplication
    @Provides
    fun providesLocationPointsStore(): LocationPointsStore {
        return LocationPointsStore()
    }
}
