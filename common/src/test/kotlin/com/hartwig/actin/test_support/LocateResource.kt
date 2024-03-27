package com.hartwig.actin.test_support

class LocateResource {
    companion object {
        fun onClasspath(requester: Any, relativePath: String): String {
            return requester.javaClass.getResource(relativePath)!!.path
        }
    }
}