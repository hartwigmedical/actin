package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.feed.FeedParseFunctions.parseDate
import com.hartwig.actin.clinical.feed.FeedParseFunctions.parseGender
import com.hartwig.actin.clinical.feed.FeedParseFunctions.parseOptionalDate
import com.hartwig.actin.clinical.feed.FeedParseFunctions.parseOptionalDouble
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class FeedParseFunctionsTest {
    @Test
    fun canParseGender() {
        Assert.assertEquals(Gender.MALE, parseGender("Male"))
        Assert.assertEquals(Gender.FEMALE, parseGender("Female"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwsExceptionOnInvalidGender() {
        parseGender("no gender")
    }

    @Test
    fun canParseDates() {
        Assert.assertNull(parseOptionalDate(Strings.EMPTY))
        val correct = LocalDate.of(2020, 10, 23)
        Assert.assertEquals(correct, parseOptionalDate("2020-10-23 13:10:55.0000000"))
        Assert.assertEquals(correct, parseOptionalDate("2020-10-23 13:10:55.000"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun crashOnInvalidDate() {
        parseDate("2020-23-10")
    }

    @Test
    fun canParseDoubles() {
        Assert.assertNull(parseOptionalDouble(Strings.EMPTY))
        Assert.assertEquals(2.1, parseOptionalDouble("2.1")!!, EPSILON)
        Assert.assertEquals(2.1, parseOptionalDouble("2,1")!!, EPSILON)
    }

    companion object {
        private const val EPSILON = 1.0E-10
    }
}