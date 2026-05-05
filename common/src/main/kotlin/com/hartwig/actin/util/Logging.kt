package com.hartwig.actin.util

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.LoggerFactory

fun KLogger.debugIndented(message: String, indent: Int = 0) {
    debug { " ".repeat(indent) + message }
}

private val logger = KotlinLogging.logger {}

fun enableDebugLogging() {
    (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger).level = Level.DEBUG
    logger.debug { "Switched root level logging to DEBUG" }
}