package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class QuestionnaireVersionTest {

    @Test
    public void canResolveVersion() {
        assertEquals(QuestionnaireVersion.V0,
                QuestionnaireVersion.version(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0())));
        assertEquals(QuestionnaireVersion.V1,
                QuestionnaireVersion.version(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1())));
    }

    @NotNull
    private static QuestionnaireEntry entry(@NotNull String questionnaire) {
        return ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .itemAnswerValueValueString(questionnaire)
                .build();
    }
}