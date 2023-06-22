package com.hartwig.actin.clinical.feed.questionnaire

import com.google.common.io.Resources
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireRawEntryMapper.Companion.createFromCurationDirectory
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.IOException

class QuestionnaireRawEntryMapperTest {
    @Test
    @Throws(IOException::class)
    fun shouldReplaceStringInQuestionnaireEntryUsingFileMapping() {
        val questionnaireRawEntryMapper = createFromCurationDirectory(Resources.getResource("curation").path)
        val (correctedQuestionnaireEntry, foundStrings) = questionnaireRawEntryMapper.correctQuestionnaireEntry("a problematic, incorrect entry")
        assertEquals("a much better entry", correctedQuestionnaireEntry)
        assertEquals(setOf("problematic, incorrect"), foundStrings)
    }
}