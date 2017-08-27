package org.projectbass.bass.core

import android.content.Context
import io.requery.Persistable
import io.requery.android.sqlite.DatabaseSource
import io.requery.reactivex.KotlinReactiveEntityStore
import io.requery.sql.KotlinEntityDataStore
import io.requery.sql.TableCreationMode
import org.projectbass.bass.BuildConfig
import org.projectbass.bass.model.Models

/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 09/08/2017
 */
class RequeryDatabase(context: Context, databaseName: String, databaseVersion: Int) : Database {

    private val mStore: KotlinReactiveEntityStore<Persistable>

    init {
        val source = DatabaseSource(context.applicationContext, Models.DEFAULT, databaseName,
                databaseVersion)
        source.setLoggingEnabled(BuildConfig.DEBUG)
        source.setTableCreationMode(TableCreationMode.CREATE_NOT_EXISTS)
        val configuration = source.configuration
        mStore = KotlinReactiveEntityStore<Persistable>(KotlinEntityDataStore(configuration))
    }

    override fun store(): KotlinReactiveEntityStore<Persistable> {
        return mStore
    }
}
