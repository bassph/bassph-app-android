package org.projectbass.bass.service

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import com.google.firebase.analytics.FirebaseAnalytics
import org.projectbass.bass.flux.action.DataCollectionActionCreator
import org.projectbass.bass.flux.model.DataCollectionModel
import org.projectbass.bass.service.job.DataCollectionJob


/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 09/07/2017
 */

class BASSJobCreator(val firebaseAnalytics: FirebaseAnalytics, val dataCollectionActionCreator: DataCollectionActionCreator, val dataCollectionModel: DataCollectionModel) : JobCreator {

    override fun create(tag: String): Job? {
        when (tag) {
            DataCollectionJob.TAG -> return DataCollectionJob(firebaseAnalytics, dataCollectionActionCreator, dataCollectionModel)
            else -> return null
        }
    }
}