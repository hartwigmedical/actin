package com.hartwig.actin.util

import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import com.hartwig.actin.util.ApplicationConfig.nonOptionalDir
import com.hartwig.actin.util.ApplicationConfig.nonOptionalFile
import com.hartwig.actin.util.ApplicationConfig.nonOptionalValue
import com.hartwig.actin.util.ApplicationConfig.optionalDir
import com.hartwig.actin.util.ApplicationConfig.optionalFile
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ApplicationConfigTest {

    private val configDirectory = resourceOnClasspath("config")
    private val configFile = resourceOnClasspath("config/file.empty")

    @Test
    fun `Should retrieve directory from config`() {
        val options = Options()
        options.addOption("directory", true, "")
        val cmd = DefaultParser().parse(options, arrayOf("-directory", configDirectory))
        assertThat(nonOptionalDir(cmd, "directory")).isEqualTo(configDirectory)
    }

    @Test(expected = ParseException::class)
    fun `Should crash on non-existing non-optional directory`() {
        val options = Options()
        options.addOption("directory", true, "")
        val cmd = DefaultParser().parse(options, arrayOf("-directory", "does not exist"))
        nonOptionalDir(cmd, "directory")
    }

    @Test
    fun `Should return null when optional directory is not provided`() {
        val options = Options()
        options.addOption("directory", true, "")
        val cmd = DefaultParser().parse(options, emptyArray())
        assertThat(optionalDir(cmd, "directory")).isNull()
    }

    @Test(expected = ParseException::class)
    fun `Should crash on non-existent optional directory`() {
        val options = Options()
        options.addOption("directory", true, "")
        val cmd = DefaultParser().parse(options, arrayOf("-directory", "/123/4332l4j/1285"))
        optionalDir(cmd, "directory")
    }

    @Test(expected = ParseException::class)
    fun `Should crash when optional directory points to file`() {
        val options = Options()
        options.addOption("directory", true, "")
        val cmd = DefaultParser().parse(options, arrayOf("-directory", configFile))
        optionalDir(cmd, "directory")
    }

    @Test
    fun `Should return existing optional directory`() {
        val options = Options()
        options.addOption("directory", true, "")
        val cmd = DefaultParser().parse(options, arrayOf("-directory", configDirectory))
        optionalDir(cmd, "directory")
    }

    @Test
    fun `Should retrieve file from config`() {
        val options = Options()
        options.addOption("file", true, "")
        val cmd = DefaultParser().parse(options, arrayOf("-file", configFile))
        assertThat(nonOptionalFile(cmd, "file")).isEqualTo(configFile)
    }

    @Test(expected = ParseException::class)
    fun `Should crash on non existing file`() {
        val options = Options()
        options.addOption("file", true, "")
        val cmd = DefaultParser().parse(options, arrayOf("-file", "does not exist"))
        nonOptionalFile(cmd, "file")
    }

    @Test
    fun `Should retrieve value from config`() {
        val options = Options()
        options.addOption("value", true, "")
        val cmd = DefaultParser().parse(options, arrayOf("-value", "value"))
        assertThat(nonOptionalValue(cmd, "value")).isEqualTo("value")
    }

    @Test(expected = ParseException::class)
    fun `Should crash on non existing value`() {
        val cmd = DefaultParser().parse(Options(), arrayOf())
        nonOptionalValue(cmd, "does not exist")
    }

    @Test
    fun `Should allow optional file to be unspecified`() {
        val options = Options()
        options.addOption("file", true, "")
        val cmd = DefaultParser().parse(options, emptyArray())
        assertThat(optionalFile(cmd, "file")).isEqualTo(null)
    }

    @Test(expected = ParseException::class)
    fun `Should crash if optional file specified but does not exist`() {
        val options = Options()
        options.addOption("file", true, "")
        val cmd = DefaultParser().parse(options, arrayOf("-file", "does not exist"))
        optionalFile(cmd, "file")
    }

    @Test
    fun `Should accept optional file if exists`() {
        val options = Options()
        options.addOption("file", true, "")
        val cmd = DefaultParser().parse(options, arrayOf("-file", configFile))
        assertThat(optionalFile(cmd, "file")).isEqualTo(configFile)
    }
}