package com.hartwig.actin.util

import com.hartwig.actin.util.Paths.forceTrailingFileSeparator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

class PathsTest {

    @Test
    fun `Should append file separator`() {
        assertThat(forceTrailingFileSeparator("hi").endsWith(File.separator)).isTrue
        val dir = "this${File.separator}dir${File.separator}"
        assertThat(forceTrailingFileSeparator(dir)).isEqualTo(dir)
    }
}