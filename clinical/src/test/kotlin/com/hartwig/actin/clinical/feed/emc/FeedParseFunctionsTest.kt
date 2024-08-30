package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.clinical.feed.emc.FeedParseFunctions.parseDate
import com.hartwig.actin.clinical.feed.emc.FeedParseFunctions.parseGender
import com.hartwig.actin.clinical.feed.emc.FeedParseFunctions.parseOptionalDate
import com.hartwig.actin.clinical.feed.emc.FeedParseFunctions.parseOptionalDouble
import com.hartwig.actin.datamodel.clinical.Gender
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test
import java.time.LocalDate

private const val EPSILON = 1.0E-10

class FeedParseFunctionsTest {

    @Test
    fun `Should parse gender`() {
        assertThat(parseGender("Male")).isEqualTo(Gender.MALE)
        assertThat(parseGender("Female")).isEqualTo(Gender.FEMALE)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception on invalid gender`() {
        parseGender("no gender")
    }

    @Test
    fun `Should parse dates`() {
        assertThat(parseOptionalDate("")).isNull()
        val correct = LocalDate.of(2020, 10, 23)
        assertThat(parseOptionalDate("2020-10-23 13:10:55.0000000")).isEqualTo(correct)
        assertThat(parseOptionalDate("2020-10-23 13:10:55.000")).isEqualTo(correct)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should crash on invalid date`() {
        parseDate("2020-23-10")
    }

    @Test
    fun `Should parse doubles`() {
        assertThat(parseOptionalDouble("")).isNull()
        assertThat(parseOptionalDouble("2.1")!!).isEqualTo(2.1, Offset.offset(EPSILON))
        assertThat(parseOptionalDouble("2,1")!!).isEqualTo(2.1, Offset.offset(EPSILON))
    }
}