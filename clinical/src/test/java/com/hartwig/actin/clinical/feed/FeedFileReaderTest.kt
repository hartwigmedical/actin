package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.feed.FeedFileReader.Companion.cleanQuotes
import org.junit.Assert
import org.junit.Test

class FeedFileReaderTest {
    @Test
    fun canCleanQuotes() {
        val input = arrayOf("\"test\"", "test", "\"test \"\" test\"", "test \" test")
        val cleaned: Array<String> = cleanQuotes(input)
        Assert.assertEquals("test", cleaned[0])
        Assert.assertEquals("test", cleaned[1])
        Assert.assertEquals("test \" test", cleaned[2])
        Assert.assertEquals("test \" test", cleaned[3])
    }
}