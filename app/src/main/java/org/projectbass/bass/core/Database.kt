package org.projectbass.bass.core

import io.requery.Persistable
import io.requery.reactivex.KotlinReactiveEntityStore

interface Database {

    /**
     * Returns the [KotlinReactiveEntityStore] for executing sql commands to the database.
     */
    fun store(): KotlinReactiveEntityStore<Persistable>
}
