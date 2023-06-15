package com.hartwig.actin.clinical.feed.questionnaire

import com.google.common.io.Resources
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireRawEntryMapper.Companion.createFromCurationDirectory
import org.junit.Assert
import org.junit.Test
import java.io.IOException

class QuestionnaireRawEntryMapperTest {
    @Test
    @Throws(IOException::class)
    fun shouldReplaceStringInQuestionnaireEntryUsingFileMapping() {
        val questionnaireRawEntryMapper = createFromCurationDirectory(Resources.getResource("curation").path)
        Assert.assertEquals("a much better entry", questionnaireRawEntryMapper.correctQuestionnaireEntry("a problematic, incorrect entry"))
    }
}