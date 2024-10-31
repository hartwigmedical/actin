package com.hartwig.actin.system.regression

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.layout.PatternLayout

class LevelRecordingAppender : AbstractAppender("TestLogAppender", null, PatternLayout.createDefaultLayout(), false, emptyArray()) {

    private val loggedLevels: MutableSet<Level> = mutableSetOf()

    override fun append(event: LogEvent) {
        loggedLevels.add(event.level)
    }

    fun levels() = loggedLevels
}

class LogLevelRecorder {
    private val logLevel = LevelRecordingAppender()

    fun start() {
        val context = LogManager.getContext(false) as LoggerContext
        logLevel.start()
        context.configuration.rootLogger.addAppender(logLevel, Level.ALL, null)
        context.updateLoggers()
    }

    fun stop() {
        val context = LogManager.getContext(false) as LoggerContext
        context.configuration.rootLogger.removeAppender("LevelRecorder")
        context.updateLoggers()
    }

    fun levelRecorded(level: Level): Boolean {
        return logLevel.levels().contains(level)
    }
}