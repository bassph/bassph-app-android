package org.projectbass.bass.model

/**
 * @author A-Ar Andrew Concepcion
 * *
 * @createdOn 11/08/2017
 */
data class Connectivity(
        val available: Boolean,
        val detailedState: String,
        val extraInfo: String,
        val failover: Boolean,
        val roaming: Boolean,
        val state: String,
        val subType: Int,
        val subTypeName: String,
        val type: Int,
        val typeName: String
)
