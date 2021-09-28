package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.apache.logging.log4j.util.Strings;
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
        QuestionnaireEntry entry = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_1());

        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);
        assertEquals("lung", questionnaire.tumorLocation());
        assertEquals("small-cell carcinoma", questionnaire.tumorType());

        List<String> treatmentHistories = questionnaire.treatmentHistoriesCurrentTumor();
        assertEquals(1, treatmentHistories.size());
        assertTrue(treatmentHistories.contains("capecitabine JAN 2020- JUL 2021"));

        List<String> otherOncologicalHistories = questionnaire.otherOncologicalHistories();
        assertEquals(1, otherOncologicalHistories.size());
        assertTrue(otherOncologicalHistories.contains("surgery JUN 2021"));

        assertEquals(TumorStage.IV, questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableLesionRecist());
        assertNull(questionnaire.hasBrainLesions());
        assertNull(questionnaire.hasActiveBrainLesions());
        assertNull(questionnaire.hasSymptomaticBrainLesions());
        assertNull(questionnaire.hasCnsLesions());
        assertNull(questionnaire.hasActiveCnsLesions());
        assertNull(questionnaire.hasSymptomaticCnsLesions());
        assertFalse(questionnaire.hasBoneLesions());
        assertFalse(questionnaire.hasLiverLesions());
        assertEquals(1, (int) questionnaire.whoStatus());
        assertFalse(questionnaire.hasSignificantCurrentInfection());
        assertNull(questionnaire.hasSignificantAberrationLatestECG());
        assertNull(questionnaire.significantAberrationLatestECG());
    }

    @Test
    public void canExtractFromQuestionnaireV1_0() {
        QuestionnaireEntry entry = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0());

        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);
        assertEquals("lung", questionnaire.tumorLocation());
        assertEquals("small-cell carcinoma", questionnaire.tumorType());

        List<String> treatmentHistories = questionnaire.treatmentHistoriesCurrentTumor();
        assertEquals(2, treatmentHistories.size());
        assertTrue(treatmentHistories.contains("Resection 2020"));
        assertTrue(treatmentHistories.contains("no systemic treatment"));

        List<String> otherOncologicalHistories = questionnaire.otherOncologicalHistories();
        assertEquals(1, otherOncologicalHistories.size());
        assertTrue(otherOncologicalHistories.contains("NA"));

        assertEquals(TumorStage.IV, questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableLesionRecist());
        assertNull(questionnaire.hasBrainLesions());
        assertNull(questionnaire.hasActiveBrainLesions());
        assertNull(questionnaire.hasSymptomaticBrainLesions());
        assertFalse(questionnaire.hasCnsLesions());
        assertNull(questionnaire.hasActiveCnsLesions());
        assertNull(questionnaire.hasSymptomaticCnsLesions());
        assertFalse(questionnaire.hasBoneLesions());
        assertFalse(questionnaire.hasLiverLesions());
        assertEquals(0, (int) questionnaire.whoStatus());
        assertFalse(questionnaire.hasSignificantCurrentInfection());
        assertNull(questionnaire.hasSignificantAberrationLatestECG());
        assertNull(questionnaire.significantAberrationLatestECG());
    }

    @Test
    public void canExtractFromQuestionnaireV0_1() {
        QuestionnaireEntry entry = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1());

        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);

        assertNull(questionnaire.tumorLocation());
        assertNull(questionnaire.tumorType());
        assertNull(questionnaire.treatmentHistoriesCurrentTumor());
        assertNull(questionnaire.otherOncologicalHistories());

        assertNull(questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableLesionRecist());
        assertNull(questionnaire.hasBrainLesions());
        assertNull(questionnaire.hasActiveBrainLesions());
        assertNull(questionnaire.hasSymptomaticBrainLesions());
        assertNull(questionnaire.hasCnsLesions());
        assertNull(questionnaire.hasActiveCnsLesions());
        assertNull(questionnaire.hasSymptomaticCnsLesions());
        assertTrue(questionnaire.hasBoneLesions());
        assertTrue(questionnaire.hasLiverLesions());
        assertEquals(1, (int) questionnaire.whoStatus());
        assertFalse(questionnaire.hasSignificantCurrentInfection());
        assertFalse(questionnaire.hasSignificantAberrationLatestECG());
        assertEquals(Strings.EMPTY, questionnaire.significantAberrationLatestECG());
    }

    @Test
    public void canExtractFromMissingOrInvalidEntry() {
        assertNull(QuestionnaireExtraction.extract(null));
        assertNull(QuestionnaireExtraction.extract(entry("Does not exist")));
    }

    @NotNull
    private static QuestionnaireEntry entry(@NotNull String questionnaire) {
        return ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .itemAnswerValueValueString(questionnaire)
                .build();
    }
}