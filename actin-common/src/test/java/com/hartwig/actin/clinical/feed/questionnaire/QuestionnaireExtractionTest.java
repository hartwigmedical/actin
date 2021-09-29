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

        List<String> treatmentHistory = questionnaire.treatmentHistoryCurrentTumor();
        assertEquals(1, treatmentHistory.size());
        assertTrue(treatmentHistory.contains("capecitabine JAN 2020- JUL 2021"));

        List<String> otherOncologicalHistory = questionnaire.otherOncologicalHistory();
        assertEquals(1, otherOncologicalHistory.size());
        assertTrue(otherOncologicalHistory.contains("surgery JUN 2021"));

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("NO"));

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
        assertTrue(questionnaire.unresolvedToxicities().isEmpty());
        assertFalse(questionnaire.hasSignificantCurrentInfection());
        assertNull(questionnaire.hasSignificantAberrationLatestECG());
        assertNull(questionnaire.significantAberrationLatestECG());

        List<String> cancerRelatedComplications = questionnaire.cancerRelatedComplications();
        assertEquals(1, cancerRelatedComplications.size());
        assertTrue(cancerRelatedComplications.contains("ascites"));
    }

    @Test
    public void canExtractFromQuestionnaireV1_0() {
        QuestionnaireEntry entry = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0());

        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);
        assertEquals("lung", questionnaire.tumorLocation());
        assertEquals("small-cell carcinoma", questionnaire.tumorType());

        List<String> treatmentHistory = questionnaire.treatmentHistoryCurrentTumor();
        assertEquals(2, treatmentHistory.size());
        assertTrue(treatmentHistory.contains("Resection 2020"));
        assertTrue(treatmentHistory.contains("no systemic treatment"));

        List<String> otherOncologicalHistory = questionnaire.otherOncologicalHistory();
        assertEquals(1, otherOncologicalHistory.size());
        assertTrue(otherOncologicalHistory.contains("NA"));

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("Migraine"));

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

        List<String> unresolvedToxicities = questionnaire.unresolvedToxicities();
        assertEquals(1, unresolvedToxicities.size());
        assertTrue(unresolvedToxicities.contains("NA"));

        assertFalse(questionnaire.hasSignificantCurrentInfection());
        assertNull(questionnaire.hasSignificantAberrationLatestECG());
        assertNull(questionnaire.significantAberrationLatestECG());

        List<String> cancerRelatedComplications = questionnaire.cancerRelatedComplications();
        assertEquals(1, cancerRelatedComplications.size());
        assertTrue(cancerRelatedComplications.contains("chronic diarrhea (likely cancer related)"));
    }

    @Test
    public void canExtractFromQuestionnaireV0_1() {
        QuestionnaireEntry entry = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1());

        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);

        assertNull(questionnaire.tumorLocation());
        assertNull(questionnaire.tumorType());
        assertNull(questionnaire.treatmentHistoryCurrentTumor());
        assertNull(questionnaire.otherOncologicalHistory());
        assertNull(questionnaire.nonOncologicalHistory());

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

        List<String> unresolvedToxicities = questionnaire.unresolvedToxicities();
        assertEquals(1, unresolvedToxicities.size());
        assertTrue(unresolvedToxicities.contains("Neuropathy GR3"));

        assertFalse(questionnaire.hasSignificantCurrentInfection());
        assertFalse(questionnaire.hasSignificantAberrationLatestECG());
        assertEquals(Strings.EMPTY, questionnaire.significantAberrationLatestECG());

        assertTrue(questionnaire.cancerRelatedComplications().isEmpty());
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