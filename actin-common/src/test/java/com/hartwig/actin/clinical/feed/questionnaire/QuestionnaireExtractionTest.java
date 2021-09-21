package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class QuestionnaireExtractionTest {

    @Test
    public void canExtractTreatmentHistoryCurrentTumor() {
        List<String> treatmentHistories =
                QuestionnaireExtraction.treatmentHistoriesCurrentTumor(TestQuestionnaireFactory.createTestQuestionnaireEntry());

        assertEquals(2, treatmentHistories.size());
        assertTrue(treatmentHistories.contains("Resection 2020"));
        assertTrue(treatmentHistories.contains("no systemic treatment"));
    }
}