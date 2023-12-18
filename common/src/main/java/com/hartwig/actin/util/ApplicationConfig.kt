package com.hartwig.actin.util

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.ParseException
import java.io.File
import java.nio.file.Files
import java.util.*

object ApplicationConfig {
    @JvmField
    val LOCALE: Locale = Locale.ENGLISH

    @JvmStatic
    @Throws(ParseException::class)
    fun nonOptionalDir(cmd: CommandLine, param: String): String {
        val value: String = nonOptionalValue(cmd, param)
        if (!pathExists(value) || !pathIsDirectory(value)) {
            throw ParseException("Parameter '" + param + "' must be an existing directory: " + value)
        }
        return value
    }

    @JvmStatic
    @Throws(ParseException::class)
    fun nonOptionalFile(cmd: CommandLine, param: String): String {
        val value: String = nonOptionalValue(cmd, param)
        if (!pathExists(value)) {
            throw ParseException("Parameter '" + param + "' must be an existing file: " + value)
        }
        return value
    }

    @JvmStatic
    @Throws(ParseException::class)
    fun nonOptionalValue(cmd: CommandLine, param: String): String {
        val value: String = cmd.getOptionValue(param)
        if (value == null) {
            throw ParseException("Parameter must be provided: " + param)
        }
        return value
    }

    private fun pathExists(path: String): Boolean {
        return Files.exists(File(path).toPath())
    }

    private fun pathIsDirectory(path: String): Boolean {
        return Files.isDirectory(File(path).toPath())
    }
}
