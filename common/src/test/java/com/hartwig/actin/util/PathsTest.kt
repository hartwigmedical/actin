package com.hartwig.actin.util

import com.hartwig.actin.util.Paths.forceTrailingFileSeparator
import org.junit.Assert
import org.junit.Test
import java.io.File

class PathsTest {
    @Test
    fun canAppendFileSeparator() {
        Assert.assertTrue(forceTrailingFileSeparator("hi").endsWith(File.separator))
        val dir = "this" + File.separator + "dir" + File.separator
        Assert.assertEquals(dir, forceTrailingFileSeparator(dir))
    }
}