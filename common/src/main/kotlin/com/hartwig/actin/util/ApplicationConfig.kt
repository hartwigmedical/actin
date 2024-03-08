package com.hartwig.actin.util

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.ParseException
import java.io.File
import java.nio.file.Files
import java.util.Locale

object ApplicationConfig {

    val LOCALE: Locale = Locale.ENGLISH

    fun nonOptionalDir(cmd: CommandLine, param: String): String {
        val value: String = nonOptionalValue(cmd, param)
        if (!pathExists(value) || !pathIsDirectory(value)) {
            throw ParseException("Parameter '$param' must be an existing directory: $value")
        }
        return value
    }

    fun optionalDir(cmd: CommandLine, param: String): String? {
        val value: String? = cmd.getOptionValue(param)
        if (value != null && !pathIsDirectory(value)) {
            throw ParseException("Parameter '$param' must be an existing directory: $value")
        }
        return value
    }
    
    fun nonOptionalFile(cmd: CommandLine, param: String): String {
        val value: String = nonOptionalValue(cmd, param)
        if (!pathExists(value)) {
            throw ParseException("Parameter '$param' must be an existing file: $value")
        }
        return value
    }

    fun nonOptionalValue(cmd: CommandLine, param: String): String {
        return cmd.getOptionValue(param) ?: throw ParseException("Parameter must be provided: $param")
    }

    fun optionalValue(cmd: CommandLine, param: String): String? {
        return cmd.getOptionValue(param)
    }

    private fun pathExists(path: String): Boolean {
        return Files.exists(File(path).toPath())
    }

    private fun pathIsDirectory(path: String): Boolean {
        return Files.isDirectory(File(path).toPath())
    }
}
