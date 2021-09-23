package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class QuestionnaireExtractionTest {

    @Test
    public void canDetermineIsActualQuestionnaire() {
        assertTrue(QuestionnaireExtraction.isActualQuestionnaire(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_1())));
        assertFalse(QuestionnaireExtraction.isActualQuestionnaire(entry("Does not exist")));
    }

    @Test
    public void canExtractFromQuestionnaireV1_1() {
        QuestionnaireEntry questionnaire = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_1());

        assertEquals("lung", QuestionnaireExtraction.tumorLocation(questionnaire));
        assertEquals("small-cell carcinoma", QuestionnaireExtraction.tumorType(questionnaire));

        List<String> treatmentHistories = QuestionnaireExtraction.treatmentHistoriesCurrentTumor(questionnaire);
        assertEquals(1, treatmentHistories.size());
        assertTrue(treatmentHistories.contains("capecitabine JAN 2020- JUL 2021"));
    }

    @Test
    public void canExtractFromQuestionnaireV1_0() {
        QuestionnaireEntry questionnaire = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0());

        assertEquals("lung", QuestionnaireExtraction.tumorLocation(questionnaire));
        assertEquals("small-cell carcinoma", QuestionnaireExtraction.tumorType(questionnaire));

        List<String> treatmentHistories = QuestionnaireExtraction.treatmentHistoriesCurrentTumor(questionnaire);
        assertEquals(2, treatmentHistories.size());
        assertTrue(treatmentHistories.contains("Resection 2020"));
        assertTrue(treatmentHistories.contains("no systemic treatment"));
    }

    @Test
    public void canExtractFromQuestionnaireV0_1() {
        QuestionnaireEntry questionnaire = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1());

        assertNull(QuestionnaireExtraction.tumorLocation(questionnaire));
        assertNull(QuestionnaireExtraction.tumorType(questionnaire));
        assertNull(QuestionnaireExtraction.treatmentHistoriesCurrentTumor(questionnaire));
    }

    @NotNull
    private static QuestionnaireEntry entry(@NotNull String questionnaire) {
        return ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .itemAnswerValueValueString(questionnaire)
                .build();
    }
}