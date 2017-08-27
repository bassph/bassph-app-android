package org.projectbass.bass.model

import io.requery.Entity
import io.requery.Key
import io.requery.Persistable

@Entity
data class History(@get:Key val testId: Long,
                   val operator: String,
                   val signal: String,
                   val bandwidth: String = "0",
                   val connectionType: String,
                   val createdDate: Long) : Persistable