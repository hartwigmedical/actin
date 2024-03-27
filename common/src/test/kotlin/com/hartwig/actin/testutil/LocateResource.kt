package com.hartwig.actin.testutil

class LocateResource(private val requester: Any) {
    fun onClasspath(relativePath: String): String {
        return requester.javaClass.getResource(relativePath)!!.path
    }
}