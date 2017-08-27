package org.projectbass.bass.inject

import org.projectbass.bass.ui.history.HistoryActivity
import org.projectbass.bass.ui.main.MainActivity
import org.projectbass.bass.ui.map.MapsActivity

import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = arrayOf(ActivityModule::class))
interface ActivityComponent {
    operator fun plus(viewModule: ViewModule): ViewComponent
    fun inject(activity: MainActivity)
    fun inject(activity: HistoryActivity)
    fun inject(mapsActivity: MapsActivity)
}
