package org.projectbass.bass.common

// Based on http://google-collections.googlecode.com/svn-history/r78/trunk/javadoc/com/google/common/base/Preconditions.html

object Preconditions {

    fun checkArgument(expression: Boolean, errorMessage: Any?) {
        if (!expression) {
            throw IllegalArgumentException(errorMessage.toString())
        }
    }

    fun checkState(expression: Boolean, errorMessage: Any?) {
        if (!expression) {
            throw IllegalStateException(errorMessage.toString())
        }
    }

    fun <T> checkNotNull(reference: T?, errorMessage: Any?): T {
        if (reference == null) {
            throw NullPointerException(errorMessage.toString())
        }
        return reference
    }

    fun <T> checkNull(reference: T?, errorMessage: Any?): T? {
        checkState(reference == null, errorMessage)
        return reference
    }
}