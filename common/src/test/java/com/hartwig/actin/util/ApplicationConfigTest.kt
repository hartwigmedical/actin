package com.hartwig.actin.util

import com.google.common.io.Resources
import com.hartwig.actin.util.ApplicationConfig.nonOptionalDir
import com.hartwig.actin.util.ApplicationConfig.nonOptionalFile
import com.hartwig.actin.util.ApplicationConfig.nonOptionalValue
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val CONFIG_DIRECTORY = Resources.getResource("config").path
private val CONFIG_FILE = Resources.getResource("config/file.empty").path

class ApplicationConfigTest {

    @Test
    fun `Should retrieve directory from config`() {
        val options = Options()
        options.addOption("directory", true, "")
        val cmd = DefaultParser().parse(options, arrayOf("-directory", CONFIG_DIRECTORY))
        assertThat(nonOptionalDir(cmd, "directory")).isEqualTo(CONFIG_DIRECTORY)
    }

    @Test(expected = ParseException::class)
    fun `Should crash on non existing directory`() {
        val options = Options()
        options.addOption("directory", true, "")
        val cmd = DefaultParser().parse(options, arrayOf("-directory", "does not exist"))
        nonOptionalDir(cmd, "directory")
    }

    @Test
    fun `Should retrieve file from config`() {
        val options = Options()
        options.addOption("file", true, "")
        val cmd = DefaultParser().parse(options, arrayOf("-file", CONFIG_FILE))
        assertThat(nonOptionalFile(cmd, "file")).isEqualTo(CONFIG_FILE)
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
}