package com.hartwig.actin.clinical.correction

import com.hartwig.actin.clinical.correction.QuestionnaireRawEntryMapper.Companion.createFromCurationDirectory
import com.hartwig.actin.testutil.ResourceLocator
import java.io.IOException
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class QuestionnaireRawEntryMapperTest {
    @Test
    @Throws(IOException::class)
    fun shouldReplaceStringInQuestionnaireEntryUsingFileMapping() {
        val questionnaireRawEntryMapper = createFromCurationDirectory(ResourceLocator(this).onClasspath("curation"))
        val correctionResult = questionnaireRawEntryMapper.correctQuestionnaireEntry("a problematic, incorrect entry")
        assertThat(correctionResult.correctedText).isEqualTo("a much better entry")
        assertThat(correctionResult.foundKeys).isEqualTo(setOf("problematic, incorrect"))
    }
}