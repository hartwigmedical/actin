package com.hartwig.actin.util

import com.google.common.io.Resources
import com.hartwig.actin.util.ApplicationConfig.nonOptionalDir
import com.hartwig.actin.util.ApplicationConfig.nonOptionalFile
import com.hartwig.actin.util.ApplicationConfig.nonOptionalValue
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class ApplicationConfigTest {
    @Test
    @Throws(ParseException::class)
    fun canRetrieveDirectoryFromConfig() {
        val options = Options()
        options.addOption("directory", true, Strings.EMPTY)
        val cmd = DefaultParser().parse(options, arrayOf("-directory", CONFIG_DIRECTORY))
        Assert.assertEquals(CONFIG_DIRECTORY, nonOptionalDir(cmd, "directory"))
    }

    @Test(expected = ParseException::class)
    @Throws(ParseException::class)
    fun crashOnNonExistingDirectory() {
        val options = Options()
        options.addOption("directory", true, Strings.EMPTY)
        val cmd = DefaultParser().parse(options, arrayOf("-directory", "does not exist"))
        nonOptionalDir(cmd, "directory")
    }

    @Test
    @Throws(ParseException::class)
    fun canRetrieveFileFromConfig() {
        val options = Options()
        options.addOption("file", true, Strings.EMPTY)
        val cmd = DefaultParser().parse(options, arrayOf("-file", CONFIG_FILE))
        Assert.assertEquals(CONFIG_FILE, nonOptionalFile(cmd, "file"))
    }

    @Test(expected = ParseException::class)
    @Throws(ParseException::class)
    fun crashOnNonExistingFile() {
        val options = Options()
        options.addOption("file", true, Strings.EMPTY)
        val cmd = DefaultParser().parse(options, arrayOf("-file", "does not exist"))
        nonOptionalFile(cmd, "file")
    }

    @Test
    @Throws(ParseException::class)
    fun canRetrieveValueFromConfig() {
        val options = Options()
        options.addOption("value", true, Strings.EMPTY)
        val cmd = DefaultParser().parse(options, arrayOf("-value", "value"))
        Assert.assertEquals("value", nonOptionalValue(cmd, "value"))
    }

    @Test(expected = ParseException::class)
    @Throws(ParseException::class)
    fun crashOnNonExistingValue() {
        val cmd = DefaultParser().parse(Options(), arrayOf())
        nonOptionalValue(cmd, "does not exist")
    }

    companion object {
        private val CONFIG_DIRECTORY = Resources.getResource("config").path
        private val CONFIG_FILE = Resources.getResource("config/file.empty").path
    }
}