package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class QuestionnaireMappingTest {

    @Test
    public void allMappingsAreComplete() {
        for (QuestionnaireKey key : QuestionnaireKey.values()) {
            assertTrue(QuestionnaireMapping.KEYS_V1_1.containsKey(key));
            assertTrue(QuestionnaireMapping.KEYS_V1_0.containsKey(key));
            assertTrue(QuestionnaireMapping.KEYS_V0_1.containsKey(key));
        }
    }

    @Test
    public void canRetrieveMappingForQuestionnaire() {
        assertEquals(QuestionnaireMapping.KEYS_V1_1,
                QuestionnaireMapping.mapping(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_1())));
        assertEquals(QuestionnaireMapping.KEYS_V1_0,
                QuestionnaireMapping.mapping(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0())));
        assertEquals(QuestionnaireMapping.KEYS_V0_1,
                QuestionnaireMapping.mapping(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1())));
    }

    @NotNull
    private static QuestionnaireEntry entry(@NotNull String questionnaire) {
        return ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .itemAnswerValueValueString(questionnaire)
                .build();
    }
}