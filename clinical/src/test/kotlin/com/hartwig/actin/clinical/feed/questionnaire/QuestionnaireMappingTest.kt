package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireKey
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireMapping
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireMapping.mapping
import com.hartwig.actin.clinical.feed.questionnaire.TestQuestionnaireFactory.entryWithText
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class QuestionnaireMappingTest {
    @Test
    fun allMappingsAreComplete() {
        for (key in QuestionnaireKey.values()) {
            assertTrue(QuestionnaireMapping.KEYS_V1_7.containsKey(key))
            assertTrue(QuestionnaireMapping.KEYS_V1_6.containsKey(key))
            assertTrue(QuestionnaireMapping.KEYS_V1_5.containsKey(key))
            assertTrue(QuestionnaireMapping.KEYS_V1_4.containsKey(key))
            assertTrue(QuestionnaireMapping.KEYS_V1_3.containsKey(key))
            assertTrue(QuestionnaireMapping.KEYS_V1_2.containsKey(key))
            assertTrue(QuestionnaireMapping.KEYS_V1_1.containsKey(key))
            assertTrue(QuestionnaireMapping.KEYS_V1_0.containsKey(key))
            assertTrue(QuestionnaireMapping.KEYS_V0_2.containsKey(key))
            assertTrue(QuestionnaireMapping.KEYS_V0_1.containsKey(key))
        }
    }

    @Test
    fun canRetrieveMappingForQuestionnaire() {
        assertMapping(QuestionnaireMapping.KEYS_V1_7, TestQuestionnaireFactory.createTestQuestionnaireValueV1_7())
        assertMapping(QuestionnaireMapping.KEYS_V1_6, TestQuestionnaireFactory.createTestQuestionnaireValueV1_6())
        assertMapping(QuestionnaireMapping.KEYS_V1_5, TestQuestionnaireFactory.createTestQuestionnaireValueV1_5())
        assertMapping(QuestionnaireMapping.KEYS_V1_4, TestQuestionnaireFactory.createTestQuestionnaireValueV1_4())
        assertMapping(QuestionnaireMapping.KEYS_V1_3, TestQuestionnaireFactory.createTestQuestionnaireValueV1_3())
        assertMapping(QuestionnaireMapping.KEYS_V1_2, TestQuestionnaireFactory.createTestQuestionnaireValueV1_2())
        assertMapping(QuestionnaireMapping.KEYS_V1_1, TestQuestionnaireFactory.createTestQuestionnaireValueV1_1())
        assertMapping(QuestionnaireMapping.KEYS_V1_0, TestQuestionnaireFactory.createTestQuestionnaireValueV1_0())
        assertMapping(QuestionnaireMapping.KEYS_V0_2, TestQuestionnaireFactory.createTestQuestionnaireValueV0_2())
        assertMapping(QuestionnaireMapping.KEYS_V0_1, TestQuestionnaireFactory.createTestQuestionnaireValueV0_1())
    }

    companion object {
        private fun assertMapping(expected: Map<QuestionnaireKey, String?>, questionnaireText: String) {
            assertEquals(expected, mapping(entryWithText(questionnaireText)))
        }
    }
}