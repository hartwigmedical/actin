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
    public void canExtractFromMissingEntry() {
        assertNull(QuestionnaireExtraction.extract(null));
    }

    @Test
    public void canParseStage() {
        assertEquals(TumorStage.IIB, QuestionnaireExtraction.toStage("IIb"));
        assertEquals(TumorStage.II, QuestionnaireExtraction.toStage("2"));
        assertEquals(TumorStage.III, QuestionnaireExtraction.toStage("3"));
        assertEquals(TumorStage.IV, QuestionnaireExtraction.toStage("4"));

        assertNull(QuestionnaireExtraction.toStage(null));
        assertNull(QuestionnaireExtraction.toStage(Strings.EMPTY));
        assertNull(QuestionnaireExtraction.toStage("not a stage"));
    }

    @Test
    public void canParseOption() {
        assertTrue(QuestionnaireExtraction.toOption("YES"));
        assertFalse(QuestionnaireExtraction.toOption("no"));

        assertNull(QuestionnaireExtraction.toOption(null));
        assertNull(QuestionnaireExtraction.toOption(Strings.EMPTY));
        assertNull(QuestionnaireExtraction.toOption("-"));
        assertNull(QuestionnaireExtraction.toOption("nvt"));
        assertNull(QuestionnaireExtraction.toOption("not an option"));
    }

    @Test
    public void canParseWHO() {
        assertEquals(1, (int) QuestionnaireExtraction.toWHO("1"));

        assertNull(QuestionnaireExtraction.toWHO(null));
        assertNull(QuestionnaireExtraction.toWHO(Strings.EMPTY));
        assertNull(QuestionnaireExtraction.toWHO("-1"));
        assertNull(QuestionnaireExtraction.toWHO("5"));
    }

    @NotNull
    private static QuestionnaireEntry entry(@NotNull String questionnaire) {
        return ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .itemAnswerValueValueString(questionnaire)
                .build();
    }
}