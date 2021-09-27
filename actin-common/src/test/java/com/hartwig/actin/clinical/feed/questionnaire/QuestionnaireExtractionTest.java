package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class QuestionnaireExtractionTest {

    @Test
    public void canDetermineIsActualQuestionnaire() {
        assertTrue(QuestionnaireExtraction.isActualQuestionnaire(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_1())));
        assertTrue(QuestionnaireExtraction.isActualQuestionnaire(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0())));
        assertTrue(QuestionnaireExtraction.isActualQuestionnaire(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1())));

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

        assertEquals(TumorStage.IV, QuestionnaireExtraction.stage(questionnaire));
        assertTrue(QuestionnaireExtraction.hasMeasurableLesionRecist(questionnaire));
        assertNull(QuestionnaireExtraction.hasBrainLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasActiveBrainLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasSymptomaticBrainLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasCnsLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasActiveCnsLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasSymptomaticCnsLesions(questionnaire));
        assertFalse(QuestionnaireExtraction.hasBoneLesions(questionnaire));
        assertFalse(QuestionnaireExtraction.hasLiverLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasOtherLesions(questionnaire));
        assertNull(QuestionnaireExtraction.otherLesions(questionnaire));
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

        assertEquals(TumorStage.IV, QuestionnaireExtraction.stage(questionnaire));
        assertTrue(QuestionnaireExtraction.hasMeasurableLesionRecist(questionnaire));
        assertNull(QuestionnaireExtraction.hasBrainLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasActiveBrainLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasSymptomaticBrainLesions(questionnaire));
        assertFalse(QuestionnaireExtraction.hasCnsLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasActiveCnsLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasSymptomaticCnsLesions(questionnaire));
        assertFalse(QuestionnaireExtraction.hasBoneLesions(questionnaire));
        assertFalse(QuestionnaireExtraction.hasLiverLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasOtherLesions(questionnaire));
        assertNull(QuestionnaireExtraction.otherLesions(questionnaire));
    }

    @Test
    public void canExtractFromQuestionnaireV0_1() {
        QuestionnaireEntry questionnaire = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1());

        assertNull(QuestionnaireExtraction.tumorLocation(questionnaire));
        assertNull(QuestionnaireExtraction.tumorType(questionnaire));
        assertNull(QuestionnaireExtraction.treatmentHistoriesCurrentTumor(questionnaire));

        assertNull(QuestionnaireExtraction.stage(questionnaire));
        assertTrue(QuestionnaireExtraction.hasMeasurableLesionRecist(questionnaire));
        assertNull(QuestionnaireExtraction.hasBrainLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasActiveBrainLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasSymptomaticBrainLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasCnsLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasActiveCnsLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasSymptomaticCnsLesions(questionnaire));
        assertTrue(QuestionnaireExtraction.hasBoneLesions(questionnaire));
        assertTrue(QuestionnaireExtraction.hasLiverLesions(questionnaire));
        assertNull(QuestionnaireExtraction.hasOtherLesions(questionnaire));
        assertNull(QuestionnaireExtraction.otherLesions(questionnaire));
    }

    @NotNull
    private static QuestionnaireEntry entry(@NotNull String questionnaire) {
        return ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .itemAnswerValueValueString(questionnaire)
                .build();
    }
}