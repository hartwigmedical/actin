package com.hartwig.actin.clinical.correction

import com.google.common.io.Resources
import com.hartwig.actin.clinical.correction.QuestionnaireRawEntryMapper.Companion.createFromCurationDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.IOException

class QuestionnaireRawEntryMapperTest {
    @Test
    @Throws(IOException::class)
    fun shouldReplaceStringInQuestionnaireEntryUsingFileMapping() {
        val questionnaireRawEntryMapper = createFromCurationDirectory(Resources.getResource("curation").path)
        val correctionResult = questionnaireRawEntryMapper.correctQuestionnaireEntry("a problematic, incorrect entry")
        assertThat(correctionResult.correctedText).isEqualTo("a much better entry")
        assertThat(correctionResult.foundKeys).isEqualTo(setOf("problematic, incorrect"))
    }
}