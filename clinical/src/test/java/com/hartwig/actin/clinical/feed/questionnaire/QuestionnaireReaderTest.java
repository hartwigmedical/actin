package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class QuestionnaireReaderTest {

    @Test
    public void canCleanQuestionnaire() {
        QuestionnaireEntry entry = entry(String.join("_", QuestionnaireReader.TERMS_TO_CLEAN) + " test");

        assertEquals("_".repeat(QuestionnaireReader.TERMS_TO_CLEAN.size() - 1) + " test", QuestionnaireReader.cleanedContents(entry));
    }

    @NotNull
    private static QuestionnaireEntry entry(@NotNull String questionnaire) {
        return ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .itemAnswerValueValueString(questionnaire)
                .build();
    }
}