package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireMapping.mapping
import com.hartwig.actin.clinical.feed.questionnaire.TestQuestionnaireFactory.entryWithText
import org.junit.Assert
import org.junit.Test

class QuestionnaireMappingTest {
    @Test
    fun allMappingsAreComplete() {
        for (key in QuestionnaireKey.values()) {
            Assert.assertTrue(QuestionnaireMapping.KEYS_V1_6.containsKey(key))
            Assert.assertTrue(QuestionnaireMapping.KEYS_V1_5.containsKey(key))
            Assert.assertTrue(QuestionnaireMapping.KEYS_V1_4.containsKey(key))
            Assert.assertTrue(QuestionnaireMapping.KEYS_V1_3.containsKey(key))
            Assert.assertTrue(QuestionnaireMapping.KEYS_V1_2.containsKey(key))
            Assert.assertTrue(QuestionnaireMapping.KEYS_V1_1.containsKey(key))
            Assert.assertTrue(QuestionnaireMapping.KEYS_V1_0.containsKey(key))
            Assert.assertTrue(QuestionnaireMapping.KEYS_V0_2.containsKey(key))
            Assert.assertTrue(QuestionnaireMapping.KEYS_V0_1.containsKey(key))
        }
    }

    @Test
    fun canRetrieveMappingForQuestionnaire() {
        Assert.assertEquals(
            QuestionnaireMapping.KEYS_V1_6,
            mapping(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_6()))
        )
        Assert.assertEquals(
            QuestionnaireMapping.KEYS_V1_5,
            mapping(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_5()))
        )
        Assert.assertEquals(
            QuestionnaireMapping.KEYS_V1_4,
            mapping(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_4()))
        )
        Assert.assertEquals(
            QuestionnaireMapping.KEYS_V1_3,
            mapping(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_3()))
        )
        Assert.assertEquals(
            QuestionnaireMapping.KEYS_V1_2,
            mapping(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_2()))
        )
        Assert.assertEquals(
            QuestionnaireMapping.KEYS_V1_1,
            mapping(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_1()))
        )
        Assert.assertEquals(
            QuestionnaireMapping.KEYS_V1_0,
            mapping(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0()))
        )
        Assert.assertEquals(
            QuestionnaireMapping.KEYS_V0_2,
            mapping(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV0_2()))
        )
        Assert.assertEquals(
            QuestionnaireMapping.KEYS_V0_1,
            mapping(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1()))
        )
    }
}