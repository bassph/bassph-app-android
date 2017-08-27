package org.projectbass.bass.inject

import android.content.Context
import android.content.res.Resources

import org.projectbass.bass.BASS
import org.projectbass.bass.core.Database
import org.projectbass.bass.core.RequeryDatabase
import org.projectbass.bass.flux.Utils
import org.projectbass.bass.flux.action.DataCollectionActionCreator
import org.projectbass.bass.flux.model.DataCollectionModel
import org.projectbass.bass.model.Sources
import org.projectbass.bass.service.BASSJobCreator

import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(private val application: BASS) {

    @Provides
    @PerApplication
    fun provideApplicationContext(): Context {
        return application
    }


    @Provides
    @PerApplication
    fun provideResources(context: Context): Resources {
        return context.resources
    }

    @Provides
    @PerApplication
    fun provideUtils(): Utils {
        return Utils()
    }

    @Provides
    @PerApplication
    fun provideSources(context: Context): Sources {
        return Sources(context)
    }

    @Provides
    @PerApplication
    fun provideBASSJobCreator(dataCollectionActionCreator: DataCollectionActionCreator,
                              dataCollectionModel: DataCollectionModel): BASSJobCreator {
        return BASSJobCreator(dataCollectionActionCreator, dataCollectionModel)
    }

    @Provides @PerApplication
    fun providesDatabase(context: Context): Database {
        return RequeryDatabase(context.applicationContext, "projectbass.db", 1)
    }
}
