package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireMapping.mapping
import com.hartwig.actin.clinical.feed.emc.questionnaire.TestQuestionnaireFactory.entryWithText
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class QuestionnaireMappingTest {

    @Test
    fun `Should have mappings for all questionnaire keys`() {
        for (key in QuestionnaireKey.entries) {
            assertThat(QuestionnaireMapping.KEYS_V1_7.containsKey(key)).isTrue()
            assertThat(QuestionnaireMapping.KEYS_V1_6.containsKey(key)).isTrue()
            assertThat(QuestionnaireMapping.KEYS_V1_5.containsKey(key)).isTrue()
            assertThat(QuestionnaireMapping.KEYS_V1_4.containsKey(key)).isTrue()
            assertThat(QuestionnaireMapping.KEYS_V1_3.containsKey(key)).isTrue()
            assertThat(QuestionnaireMapping.KEYS_V1_2.containsKey(key)).isTrue()
            assertThat(QuestionnaireMapping.KEYS_V1_1.containsKey(key)).isTrue()
            assertThat(QuestionnaireMapping.KEYS_V1_0.containsKey(key)).isTrue()
            assertThat(QuestionnaireMapping.KEYS_V0_2.containsKey(key)).isTrue()
            assertThat(QuestionnaireMapping.KEYS_V0_1.containsKey(key)).isTrue()
        }
    }

    @Test
    fun `Should have mappings for every questionnaire version`() {
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

    private fun assertMapping(expected: Map<QuestionnaireKey, String?>, questionnaireText: String) {
        assertThat(mapping(entryWithText(questionnaireText))).isEqualTo(expected)
    }
}