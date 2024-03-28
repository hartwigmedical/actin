package com.hartwig.actin.testutil

object ResourceLocator

fun resourceOnClasspath(relativePath: String): String {
    return ResourceLocator.javaClass.getResource("/" + relativePath.removePrefix("/"))!!.path
}