package org.projectbass.bass.inject

import org.projectbass.bass.BASS
import dagger.Component

@PerApplication
@Component(modules = arrayOf(ApplicationModule::class, RestModule::class, ActionCreatorModule::class, ModelModule::class, StoreModule::class))
interface ApplicationComponent {
    fun plus(activityModule: ActivityModule): ActivityComponent
    fun inject(app: BASS)

}
