package org.projectbass.bass.flux

/**
 * @author A-Ar Andrew Concepcion
 */
interface AliveUiThread {

    /**
     * Runs the [Runnable] if the current context is alive.
     */
    fun runOnUiThreadIfAlive(runnable: Runnable)

    /**
     * Runs the [Runnable] if the current context is alive.
     */
    fun runOnUiThreadIfAlive(runnable: Runnable, delayMillis: Long)
}
