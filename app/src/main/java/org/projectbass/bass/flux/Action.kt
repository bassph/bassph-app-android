package org.projectbass.bass.flux

/**
 * @author A-Ar Andrew Concepcion
 */
class Action(private val type: String, private val data: Any?) {

    /**
     * Type of the action
     */
    fun type(): String {
        return type
    }

    /**
     * Payload of the action
     */
    fun data(): Any? {
        return data
    }

    companion object {

        val ACTION_NO_ACTION = "ACTION_NO_ACTION"

        fun create(type: String, data: Any?): Action {
            return Action(type, data)
        }
    }
}
