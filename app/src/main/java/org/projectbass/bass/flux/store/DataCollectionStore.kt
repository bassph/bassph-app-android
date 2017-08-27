package org.projectbass.bass.flux.store

import org.projectbass.bass.flux.Action
import org.projectbass.bass.flux.AppError
import org.projectbass.bass.flux.Store
import org.projectbass.bass.flux.action.DataCollectionActionCreator
import org.projectbass.bass.model.Data
import rx.Observable

/**
 * @author A-Ar Andrew Concepcion
 */
class DataCollectionStore : Store<DataCollectionStore>() {
    var data: Data? = null
        private set
    var action: String? = null
        private set
    var error: AppError? = null
        private set

    fun observableWithFilter(filter: String): Observable<DataCollectionStore> {
        return observable().filter { store -> filter == store.action }
    }

    private fun updateState() {
        error = null
    }

    private fun updateData(action: Action) {
        if (action.data() is Data) {
            data = action.data() as Data?
        }
    }

    private fun updateError(action: Action) {
        if (action.data() != null && action.data() is AppError) {
            error = action.data() as AppError?
        }
    }

    private fun updateAction(action: Action) {
        this.action = action.type()
    }

    override fun onReceiveAction(action: Action) {
        updateAction(action)
        when (action.type()) {
            DataCollectionActionCreator.ACTION_COLLECT_DATA_S -> {
                updateState()
                updateData(action)
                notifyStoreChanged(this)
            }
            DataCollectionActionCreator.ACTION_COLLECT_DATA_F -> {
                updateError(action)
                notifyStoreChanged(this)
            }
            DataCollectionActionCreator.ACTION_SEND_DATA_S -> {
                updateState()
                updateData(action)
                notifyStoreChanged(this)
            }
            DataCollectionActionCreator.ACTION_SEND_DATA_F -> {
                updateError(action)
                notifyStoreChanged(this)
            }
        }
    }
}
