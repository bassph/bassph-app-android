package org.projectbass.bass.ui

import android.os.Bundle
import android.support.annotation.LayoutRes
import butterknife.ButterKnife
import org.projectbass.bass.BASS
import org.projectbass.bass.flux.FluxActivity
import org.projectbass.bass.inject.ActivityComponent
import org.projectbass.bass.inject.ActivityModule

/**
 * Paul Sydney Orozco (@xtrycatchx) on 4/2/17.
 */

abstract class BaseActivity : FluxActivity() {

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        setContentView(layoutRes)
        ButterKnife.bind(this)
    }

    val activityComponent: ActivityComponent
        get() {
            val applicationComponent = (application as BASS).applicationComponent
            return applicationComponent.plus(ActivityModule(this))
        }

    @get:LayoutRes
    protected abstract val layoutRes: Int


}
