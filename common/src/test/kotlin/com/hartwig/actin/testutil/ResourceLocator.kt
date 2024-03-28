package com.hartwig.actin.testutil

class ResourceLocator(private val requester: Any? = null) {
    fun onClasspath(relativePath: String): String {
        val clazz = requester ?: this
        return clazz.javaClass.getResource("/" + relativePath.removePrefix("/"))!!.path
    }
}