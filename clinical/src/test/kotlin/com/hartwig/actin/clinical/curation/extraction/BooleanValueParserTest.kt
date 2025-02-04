package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.extraction.BooleanValueParser.parseBoolean
import com.hartwig.actin.util.Either.Left
import com.hartwig.actin.util.Either.Right
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BooleanValueParserTest {

    @Test
    fun `Should return Left for unknown inputs`() {
        val input = "unexpected"
        val curated = parseBoolean(input)
        assertThat(curated.isLeft).isTrue
        assertThat((curated as Left).value).isEqualTo(input)
    }

    @Test
    fun `Should curate options without taking into account the case`() {
        assertExpectedExtraction("SusPectEd", null)
        assertExpectedExtraction("yEs", true)
        assertExpectedExtraction("botaantasting BIJ weke DELEN massa", false)
    }

    private fun assertExpectedExtraction(input: String, expected: Boolean?) {
        val curated = parseBoolean(input)
        assertThat(curated.isLeft).isFalse
        assertThat((curated as Right).value).isEqualTo(expected)
    }
}