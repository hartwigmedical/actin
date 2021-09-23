package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class QuestionnaireMappingTest {

    @Test
    public void allMappingsAreComplete() {
        for (QuestionnaireKey key : QuestionnaireKey.values()) {
            assertTrue(QuestionnaireMapping.KEYS_V1_0B.containsKey(key));
            assertTrue(QuestionnaireMapping.KEYS_V1_0A.containsKey(key));
            assertTrue(QuestionnaireMapping.KEYS_V0.containsKey(key));
        }
    }

    @Test
    public void canRetrieveMappingForQuestionnaire() {
        assertEquals(QuestionnaireMapping.KEYS_V1_0B,
                QuestionnaireMapping.mapping(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0B())));
        assertEquals(QuestionnaireMapping.KEYS_V1_0A,
                QuestionnaireMapping.mapping(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0A())));
        assertEquals(QuestionnaireMapping.KEYS_V0,
                QuestionnaireMapping.mapping(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0())));
    }

    @NotNull
    private static QuestionnaireEntry entry(@NotNull String questionnaire) {
        return ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .itemAnswerValueValueString(questionnaire)
                .build();
    }
}