package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class QuestionnaireVersionTest {

    @Test
    public void canResolveAllVersions() {
        assertVersion(QuestionnaireVersion.V1_5, TestQuestionnaireFactory.createTestQuestionnaireValueV1_5());
        assertVersion(QuestionnaireVersion.V1_4, TestQuestionnaireFactory.createTestQuestionnaireValueV1_4());
        assertVersion(QuestionnaireVersion.V1_3, TestQuestionnaireFactory.createTestQuestionnaireValueV1_3());
        assertVersion(QuestionnaireVersion.V1_2, TestQuestionnaireFactory.createTestQuestionnaireValueV1_2());
        assertVersion(QuestionnaireVersion.V1_1, TestQuestionnaireFactory.createTestQuestionnaireValueV1_1());
        assertVersion(QuestionnaireVersion.V1_0, TestQuestionnaireFactory.createTestQuestionnaireValueV1_0());
        assertVersion(QuestionnaireVersion.V0_2, TestQuestionnaireFactory.createTestQuestionnaireValueV0_2());
        assertVersion(QuestionnaireVersion.V0_1, TestQuestionnaireFactory.createTestQuestionnaireValueV0_1());
    }

    private static void assertVersion(@NotNull QuestionnaireVersion expected, @NotNull String questionnaire) {
        assertEquals(expected, QuestionnaireVersion.version(entry(questionnaire)));
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnUnresolvedVersion() {
        QuestionnaireVersion.version(entry("Not an entry"));
    }

    @NotNull
    private static QuestionnaireEntry entry(@NotNull String questionnaire) {
        return ImmutableQuestionnaireEntry.builder().from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .itemAnswerValueValueString(questionnaire)
                .build();
    }
}