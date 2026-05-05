package com.hartwig.actin.util

import io.github.oshai.kotlinlogging.KLogger

fun KLogger.debugIndented(message: String, indent: Int = 0) {
    debug { " ".repeat(indent) + message }
}