package org.projectbass.bass

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.evernote.android.job.JobManager
import com.facebook.stetho.Stetho
import jonathanfinerty.once.Once
import org.projectbass.bass.inject.ApplicationComponent
import org.projectbass.bass.inject.ApplicationModule
import org.projectbass.bass.inject.DaggerApplicationComponent
import org.projectbass.bass.inject.RestModule
import javax.inject.Inject

/**
 * Paul Sydney Orozco (@xtrycatchx) on 4/2/17.

 */

class BASS : Application() {

    lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        Once.initialise(this)
        Stetho.initializeWithDefaults(this)

        this.applicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(ApplicationModule(this))
                .restModule(RestModule())
                .build()
        applicationComponent.inject(this)
    }

    public override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        try {
            MultiDex.install(this)
        } catch (ignore: RuntimeException) {
            // Multidex support doesn't play well with Robolectric yet
        }

    }

}