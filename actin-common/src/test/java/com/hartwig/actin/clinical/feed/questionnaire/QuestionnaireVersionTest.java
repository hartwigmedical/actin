package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class QuestionnaireVersionTest {

    @Test
    public void canResolveAllVersions() {
        assertEquals(QuestionnaireVersion.V1_0B,
                QuestionnaireVersion.version(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0B())));
        assertEquals(QuestionnaireVersion.V1_0A,
                QuestionnaireVersion.version(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0A())));
        assertEquals(QuestionnaireVersion.V0,
                QuestionnaireVersion.version(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0())));
    }

    @NotNull
    private static QuestionnaireEntry entry(@NotNull String questionnaire) {
        return ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .itemAnswerValueValueString(questionnaire)
                .build();
    }
}