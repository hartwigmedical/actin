package com.hartwig.actin.util

import com.hartwig.actin.util.Paths.forceTrailingFileSeparator
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PathsTest {

    @Test
    fun `Should append file separator`() {
        assertThat(forceTrailingFileSeparator("hi").endsWith(File.separator)).isTrue
        val dir = "this${File.separator}dir${File.separator}"
        assertThat(forceTrailingFileSeparator(dir)).isEqualTo(dir)
    }
}