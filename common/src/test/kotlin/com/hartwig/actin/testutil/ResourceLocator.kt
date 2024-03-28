package com.hartwig.actin.testutil

class ResourceLocator() {
    fun onClasspath(relativePath: String): String {
        return this.javaClass.getResource("/" + relativePath.removePrefix("/"))!!.path
    }
}