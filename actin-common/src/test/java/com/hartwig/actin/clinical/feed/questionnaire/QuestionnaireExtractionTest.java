package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class QuestionnaireExtractionTest {

    @Test
    public void canDetermineIsActualQuestionnaire() {
        QuestionnaireEntry correct = TestQuestionnaireFactory.createTestQuestionnaireEntry();
        assertTrue(QuestionnaireExtraction.isActualQuestionnaire(correct));
        assertFalse(QuestionnaireExtraction.isActualQuestionnaire(ImmutableQuestionnaireEntry.builder()
                .from(correct)
                .itemAnswerValueValueString(Strings.EMPTY)
                .build()));
    }

    @Test
    public void canExtractTreatmentHistoryCurrentTumor() {
        List<String> treatmentHistories =
                QuestionnaireExtraction.treatmentHistoriesCurrentTumor(TestQuestionnaireFactory.createTestQuestionnaireEntry());

        assertEquals(2, treatmentHistories.size());
        assertTrue(treatmentHistories.contains("Resection 2020"));
        assertTrue(treatmentHistories.contains("no systemic treatment"));
    }

    @Test
    public void canExtractFromQuestionnaireV0() {
        QuestionnaireEntry entryV0 = ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .itemAnswerValueValueString(TestQuestionnaireFactory.createTestQuestionnaireValueV0())
                .build();

        assertTrue(QuestionnaireExtraction.treatmentHistoriesCurrentTumor(entryV0).isEmpty());
    }
}