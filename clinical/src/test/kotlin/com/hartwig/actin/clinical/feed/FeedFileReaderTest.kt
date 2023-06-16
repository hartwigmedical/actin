package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.feed.FeedFileReader.Companion.cleanQuotes
import junit.framework.TestCase.assertEquals
import org.junit.Test

class FeedFileReaderTest {
    @Test
    fun canCleanQuotes() {
        val input = arrayOf("\"test\"", "test", "\"test \"\" test\"", "test \" test")
        val cleaned: Array<String> = cleanQuotes(input)
        assertEquals("test", cleaned[0])
        assertEquals("test", cleaned[1])
        assertEquals("test \" test", cleaned[2])
        assertEquals("test \" test", cleaned[3])
    }
}