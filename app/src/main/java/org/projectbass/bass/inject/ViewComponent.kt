package org.projectbass.bass.inject

import dagger.Subcomponent

@PerView
@Subcomponent(modules = arrayOf(ViewModule::class))
interface ViewComponent