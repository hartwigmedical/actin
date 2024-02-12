package com.hartwig.actin.clinical.correction

import com.google.common.io.Resources
import com.hartwig.actin.clinical.feed.emc.questionnaire.TestQuestionnaireFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class QuestionnaireCorrectionTest {

    @Test
    fun shouldCorrectQuestionnaireEntriesAndReportFoundKeys() {
        val questionnaireEntries =
            listOf("a problematic, incorrect entry", "an entry with a bunch of issues").map(TestQuestionnaireFactory::entryWithText)

        val questionnaireRawEntryMapper = QuestionnaireRawEntryMapper.createFromCurationDirectory(Resources.getResource("curation").path)

        assertThat(QuestionnaireCorrection.correctQuestionnaires(questionnaireEntries, questionnaireRawEntryMapper)).isEqualTo(
            listOf("a much better entry", "an entry with no problems").map(TestQuestionnaireFactory::entryWithText)
        )
    }
}