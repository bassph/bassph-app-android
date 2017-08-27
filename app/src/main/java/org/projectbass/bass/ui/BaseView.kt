package org.projectbass.bass.ui

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.LayoutRes
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import butterknife.ButterKnife
import org.projectbass.bass.inject.ViewComponent
import org.projectbass.bass.inject.ViewModule

/**
 * Paul Sydney Orozco (@xtrycatchx) on 4/2/17.
 */

abstract class BaseView : FrameLayout {

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        onSaveInstanceStateBundle(bundle)
        bundle.putParcelable("instanceState", super.onSaveInstanceState())
        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        var state = state
        if (state is Bundle) {
            val bundle = state
            state = bundle.getParcelable<Parcelable>("instanceState")
            onRestoreInstanceStateBundle(bundle)
        }
        super.onRestoreInstanceState(state)
    }

    @get:LayoutRes
    protected abstract val layoutRes: Int

    protected fun onSaveInstanceStateBundle(bundle: Bundle) {}

    protected fun onRestoreInstanceStateBundle(bundle: Bundle) {}

    protected val viewComponent: ViewComponent
        get() {
            val activity = context as BaseActivity
            return activity.activityComponent.plus(ViewModule(this))
        }

    private fun init(context: Context) {
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        View.inflate(context, layoutRes, this)
        ButterKnife.bind(this)
    }
}