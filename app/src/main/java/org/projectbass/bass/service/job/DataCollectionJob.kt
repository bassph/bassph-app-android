package org.projectbass.bass.service.job

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import org.projectbass.bass.flux.action.DataCollectionActionCreator
import org.projectbass.bass.flux.model.DataCollectionModel
import org.projectbass.bass.model.Data
import org.projectbass.bass.utils.SharedPrefUtil
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit


/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 09/07/2017
 */

class DataCollectionJob(val dataCollectionActionCreator: DataCollectionActionCreator, val dataCollectionModel: DataCollectionModel) : Job() {

    override fun onRunJob(params: Params): Job.Result {
        if (!SharedPrefUtil.retrieveFlag(context, "auto_measure")) return Job.Result.SUCCESS
        var data: Data?
        try {
            data = dataCollectionModel.executeNetworkTest().toBlocking().toFuture().get()
        } catch (e: InterruptedException) {
            return Job.Result.RESCHEDULE
        } catch (e: ExecutionException) {
            return Job.Result.RESCHEDULE
        }

        data?.let {
            dataCollectionActionCreator.sendData(it)
        }

        return Job.Result.SUCCESS
    }

    companion object {

        val TAG = "DataCollectionJob"

        fun scheduleJob() {
            JobRequest.Builder(DataCollectionJob.TAG)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(30), TimeUnit.MINUTES.toMillis(5))
                    .setPersisted(true)
                    .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                    .setUpdateCurrent(true)
                    .build()
                    .schedule()
        }
    }
}