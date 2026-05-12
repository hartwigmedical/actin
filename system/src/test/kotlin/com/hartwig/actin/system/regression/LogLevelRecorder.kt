package com.hartwig.actin.system.regression

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.slf4j.LoggerFactory

class LevelRecordingAppender : AppenderBase<ILoggingEvent>() {

    private val loggedLevels: MutableSet<Level> = mutableSetOf()

    override fun append(event: ILoggingEvent) {
        if (event.loggerName.startsWith("com.hartwig.actin")) {
            loggedLevels.add(event.level)
        }
    }

    fun levels() = loggedLevels
}

class LogLevelRecorder {

    private val logLevel = LevelRecordingAppender()

    fun start() {
        val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        logLevel.context = rootLogger.loggerContext
        logLevel.start()
        rootLogger.addAppender(logLevel)
    }

    fun stop() {
        val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        rootLogger.detachAppender(logLevel)
        logLevel.stop()
    }

    fun levelRecorded(level: Level): Boolean {
        return logLevel.levels().contains(level)
    }
}
