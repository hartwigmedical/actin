package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.extraction.BooleanValueParser.isTrue
import com.hartwig.actin.clinical.curation.extraction.BooleanValueParser.isUnknown
import com.hartwig.actin.clinical.curation.extraction.BooleanValueParser.parseBoolean
import com.hartwig.actin.util.Either.Left
import com.hartwig.actin.util.Either.Right
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val UNCERTAIN_INPUT = "SusPectEd"
private const val TRUE_INPUT = "yEs"
private const val FALSE_INPUT = "botaantasting BIJ weke DELEN massa"
private const val UNEXPECTED_INPUT = "unexpected"

class BooleanValueParserTest {

    @Test
    fun `Should return Left for unknown inputs`() {
        val input = UNEXPECTED_INPUT
        val curated = parseBoolean(input)
        assertThat(curated.isLeft).isTrue
        assertThat((curated as Left).value).isEqualTo(input)
    }

    @Test
    fun `Should curate options without taking into account the case`() {
        assertExpectedExtraction(UNCERTAIN_INPUT, null)
        assertExpectedExtraction(TRUE_INPUT, true)
        assertExpectedExtraction(FALSE_INPUT, false)
    }

    @Test
    fun `Should identify if text represents a boolean true`() {
        assertThat(isTrue(UNCERTAIN_INPUT)).isFalse
        assertThat(isTrue(TRUE_INPUT)).isTrue
        assertThat(isTrue(FALSE_INPUT)).isFalse
        assertThat(isTrue(UNEXPECTED_INPUT)).isFalse
    }

    @Test
    fun `Should identify if text represents an unknown`() {
        assertThat(isUnknown(UNCERTAIN_INPUT)).isTrue
        assertThat(isUnknown(TRUE_INPUT)).isFalse
        assertThat(isUnknown(FALSE_INPUT)).isFalse
        assertThat(isUnknown(UNEXPECTED_INPUT)).isFalse
    }

    private fun assertExpectedExtraction(input: String, expected: Boolean?) {
        val curated = parseBoolean(input)
        assertThat(curated.isLeft).isFalse
        assertThat((curated as Right).value).isEqualTo(expected)
    }
}