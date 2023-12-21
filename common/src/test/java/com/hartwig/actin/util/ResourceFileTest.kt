package com.hartwig.actin.util

import com.hartwig.actin.util.ResourceFile.bool
import com.hartwig.actin.util.ResourceFile.optionalBool
import com.hartwig.actin.util.ResourceFile.optionalDate
import com.hartwig.actin.util.ResourceFile.optionalInteger
import com.hartwig.actin.util.ResourceFile.optionalNumber
import com.hartwig.actin.util.ResourceFile.optionalString
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class ResourceFileTest {
    @Test
    fun canParseStrings() {
        Assert.assertNull(optionalString(Strings.EMPTY))
        Assert.assertEquals("hi", optionalString("hi"))
    }

    @Test
    fun canParseBooleans() {
        Assert.assertNull(optionalBool("unknown"))
        Assert.assertNull(optionalBool(Strings.EMPTY))
        Assert.assertTrue(optionalBool("1")!!)
        Assert.assertFalse(optionalBool("0")!!)
    }

    @Test(expected = IllegalArgumentException::class)
    fun crashOnInvalidBoolean() {
        bool("True")
    }

    @Test
    fun canParseDates() {
        Assert.assertNull(optionalDate(Strings.EMPTY))
        Assert.assertEquals(LocalDate.of(2019, 4, 20), optionalDate("2019-04-20"))
    }

    @Test
    fun canParseIntegers() {
        Assert.assertNull(optionalInteger(Strings.EMPTY))
        Assert.assertEquals(4, (optionalInteger("4") as Int).toLong())
    }

    @Test
    fun canParseDoubles() {
        Assert.assertNull(optionalNumber(Strings.EMPTY))
        Assert.assertEquals(4.2, optionalNumber("4.2")!!, EPSILON)
    }

    companion object {
        private const val EPSILON = 1.0E-10
    }
}