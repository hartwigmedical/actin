package com.hartwig.actin.clinical.feed.questionnaire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class QuestionnaireExtractionTest {

    @Test
    public void canDetermineIsActualQuestionnaire() {
        assertTrue(QuestionnaireExtraction.isActualQuestionnaire(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_5())));
        assertTrue(QuestionnaireExtraction.isActualQuestionnaire(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_4())));
        assertTrue(QuestionnaireExtraction.isActualQuestionnaire(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_3())));
        assertTrue(QuestionnaireExtraction.isActualQuestionnaire(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_2())));
        assertTrue(QuestionnaireExtraction.isActualQuestionnaire(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_1())));
        assertTrue(QuestionnaireExtraction.isActualQuestionnaire(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0())));
        assertTrue(QuestionnaireExtraction.isActualQuestionnaire(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0_2())));
        assertTrue(QuestionnaireExtraction.isActualQuestionnaire(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1())));

        assertFalse(QuestionnaireExtraction.isActualQuestionnaire(entry("Does not exist")));
    }

    @Test
    public void canExtractFromQuestionnaireV1_5() {
        QuestionnaireEntry entry = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_5());

        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);

        assertEquals(LocalDate.of(2020, 8, 28), questionnaire.date());
        assertEquals("ovary", questionnaire.tumorLocation());
        assertEquals("serous", questionnaire.tumorType());
        assertEquals("lymph node", questionnaire.biopsyLocation());

        List<String> treatmentHistory = questionnaire.treatmentHistoryCurrentTumor();
        assertEquals(2, treatmentHistory.size());
        assertTrue(treatmentHistory.contains("cisplatin"));
        assertTrue(treatmentHistory.contains("nivolumab"));

        List<String> otherOncologicalHistory = questionnaire.otherOncologicalHistory();
        assertEquals(1, otherOncologicalHistory.size());
        assertTrue(otherOncologicalHistory.contains("surgery"));

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("diabetes"));

        List<String> molecularTests = questionnaire.molecularTests();
        assertEquals(1, molecularTests.size());
        assertTrue(molecularTests.contains("ERBB2 3+"));

        assertEquals(TumorStage.IV, questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableDisease());
        assertTrue(questionnaire.hasBrainLesions());
        assertTrue(questionnaire.hasActiveBrainLesions());
        assertNull(questionnaire.hasCnsLesions());
        assertNull(questionnaire.hasActiveCnsLesions());
        assertFalse(questionnaire.hasBoneLesions());
        assertFalse(questionnaire.hasLiverLesions());

        List<String> otherLesions = questionnaire.otherLesions();
        assertEquals(1, otherLesions.size());
        assertTrue(otherLesions.contains("pulmonal"));

        assertEquals(0, (int) questionnaire.whoStatus());
        List<String> unresolvedToxicities = questionnaire.unresolvedToxicities();
        assertEquals(1, unresolvedToxicities.size());
        assertTrue(unresolvedToxicities.contains("toxic"));

        InfectionStatus infectionStatus = questionnaire.infectionStatus();
        assertNotNull(infectionStatus);
        assertFalse(infectionStatus.hasActiveInfection());

        ECG ecg = questionnaire.ecg();
        assertNotNull(ecg);
        assertTrue(ecg.hasSigAberrationLatestECG());
        assertEquals("Sinus", ecg.aberrationDescription());

        List<String> complications = questionnaire.complications();
        assertEquals(1, complications.size());
        assertTrue(complications.contains("vomit"));
    }

    @Test
    public void canExtractFromQuestionnaireV1_4() {
        QuestionnaireEntry entry = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_4());

        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);

        assertEquals(LocalDate.of(2020, 8, 28), questionnaire.date());
        assertEquals("ovary", questionnaire.tumorLocation());
        assertEquals("serous", questionnaire.tumorType());
        assertEquals("Lymph node", questionnaire.biopsyLocation());

        List<String> treatmentHistory = questionnaire.treatmentHistoryCurrentTumor();
        assertEquals(2, treatmentHistory.size());
        assertTrue(treatmentHistory.contains("cisplatin"));
        assertTrue(treatmentHistory.contains("nivolumab"));

        List<String> otherOncologicalHistory = questionnaire.otherOncologicalHistory();
        assertEquals(1, otherOncologicalHistory.size());
        assertTrue(otherOncologicalHistory.contains("surgery"));

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("diabetes"));

        List<String> molecularTests = questionnaire.molecularTests();
        assertEquals(1, molecularTests.size());
        assertTrue(molecularTests.contains("IHC ERBB2 3+"));

        assertEquals(TumorStage.III, questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableDisease());
        assertNull(questionnaire.hasBrainLesions());
        assertNull(questionnaire.hasActiveBrainLesions());
        assertNull(questionnaire.hasCnsLesions());
        assertNull(questionnaire.hasActiveCnsLesions());
        assertFalse(questionnaire.hasBoneLesions());
        assertFalse(questionnaire.hasLiverLesions());

        List<String> otherLesions = questionnaire.otherLesions();
        assertEquals(1, otherLesions.size());
        assertTrue(otherLesions.contains("pulmonal"));

        assertEquals(0, (int) questionnaire.whoStatus());
        assertTrue(questionnaire.unresolvedToxicities().isEmpty());

        InfectionStatus infectionStatus = questionnaire.infectionStatus();
        assertNotNull(infectionStatus);
        assertFalse(infectionStatus.hasActiveInfection());

        ECG ecg = questionnaire.ecg();
        assertNotNull(ecg);
        assertTrue(ecg.hasSigAberrationLatestECG());
        assertEquals("Sinus", ecg.aberrationDescription());

        List<String> complications = questionnaire.complications();
        assertEquals(1, complications.size());
        assertTrue(complications.contains("nausea"));
    }

    @Test
    public void canExtractFromQuestionnaireV1_3() {
        QuestionnaireEntry entry = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_3());

        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);

        assertEquals(LocalDate.of(2020, 8, 28), questionnaire.date());
        assertEquals("ovary", questionnaire.tumorLocation());
        assertEquals("serous", questionnaire.tumorType());
        assertEquals("Lymph node", questionnaire.biopsyLocation());

        List<String> treatmentHistory = questionnaire.treatmentHistoryCurrentTumor();
        assertEquals(2, treatmentHistory.size());
        assertTrue(treatmentHistory.contains("cisplatin"));
        assertTrue(treatmentHistory.contains("nivolumab"));

        List<String> otherOncologicalHistory = questionnaire.otherOncologicalHistory();
        assertEquals(1, otherOncologicalHistory.size());
        assertTrue(otherOncologicalHistory.contains("surgery"));

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("diabetes"));

        assertNull(questionnaire.molecularTests());

        assertEquals(TumorStage.III, questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableDisease());
        assertNull(questionnaire.hasBrainLesions());
        assertNull(questionnaire.hasActiveBrainLesions());
        assertNull(questionnaire.hasCnsLesions());
        assertNull(questionnaire.hasActiveCnsLesions());
        assertFalse(questionnaire.hasBoneLesions());
        assertFalse(questionnaire.hasLiverLesions());

        List<String> otherLesions = questionnaire.otherLesions();
        assertEquals(1, otherLesions.size());
        assertTrue(otherLesions.contains("pulmonal"));

        assertEquals(0, (int) questionnaire.whoStatus());
        assertTrue(questionnaire.unresolvedToxicities().isEmpty());

        InfectionStatus infectionStatus = questionnaire.infectionStatus();
        assertNotNull(infectionStatus);
        assertFalse(infectionStatus.hasActiveInfection());

        ECG ecg = questionnaire.ecg();
        assertNotNull(ecg);
        assertTrue(ecg.hasSigAberrationLatestECG());
        assertEquals("Sinus", ecg.aberrationDescription());

        List<String> complications = questionnaire.complications();
        assertEquals(1, complications.size());
        assertTrue(complications.contains("nausea"));
    }

    @Test
    public void canExtractFromQuestionnaireV1_2() {
        QuestionnaireEntry entry = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_2());

        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);

        assertEquals(LocalDate.of(2020, 8, 28), questionnaire.date());
        assertEquals("ovary", questionnaire.tumorLocation());
        assertEquals("serous", questionnaire.tumorType());
        assertEquals("Lymph node", questionnaire.biopsyLocation());

        List<String> treatmentHistory = questionnaire.treatmentHistoryCurrentTumor();
        assertEquals(2, treatmentHistory.size());
        assertTrue(treatmentHistory.contains("cisplatin"));
        assertTrue(treatmentHistory.contains("nivolumab"));

        List<String> otherOncologicalHistory = questionnaire.otherOncologicalHistory();
        assertEquals(1, otherOncologicalHistory.size());
        assertTrue(otherOncologicalHistory.contains("surgery"));

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("diabetes"));

        assertNull(questionnaire.molecularTests());

        assertEquals(TumorStage.III, questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableDisease());
        assertNull(questionnaire.hasBrainLesions());
        assertNull(questionnaire.hasActiveBrainLesions());
        assertNull(questionnaire.hasCnsLesions());
        assertNull(questionnaire.hasActiveCnsLesions());
        assertFalse(questionnaire.hasBoneLesions());
        assertFalse(questionnaire.hasLiverLesions());

        List<String> otherLesions = questionnaire.otherLesions();
        assertEquals(1, otherLesions.size());
        assertTrue(otherLesions.contains("pulmonal"));

        assertEquals(0, (int) questionnaire.whoStatus());
        assertTrue(questionnaire.unresolvedToxicities().isEmpty());

        InfectionStatus infectionStatus = questionnaire.infectionStatus();
        assertNotNull(infectionStatus);
        assertFalse(infectionStatus.hasActiveInfection());

        ECG ecg = questionnaire.ecg();
        assertNotNull(ecg);
        assertTrue(ecg.hasSigAberrationLatestECG());
        assertEquals("Sinus", ecg.aberrationDescription());

        List<String> complications = questionnaire.complications();
        assertEquals(1, complications.size());
        assertTrue(complications.contains("nausea"));
    }

    @Test
    public void canExtractFromQuestionnaireV1_1() {
        QuestionnaireEntry entry = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_1());

        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);

        assertEquals(LocalDate.of(2020, 8, 28), questionnaire.date());
        assertEquals("ovary", questionnaire.tumorLocation());
        assertEquals("serous", questionnaire.tumorType());
        assertEquals("Lymph node", questionnaire.biopsyLocation());

        List<String> treatmentHistory = questionnaire.treatmentHistoryCurrentTumor();
        assertEquals(2, treatmentHistory.size());
        assertTrue(treatmentHistory.contains("cisplatin"));
        assertTrue(treatmentHistory.contains("nivolumab"));

        List<String> otherOncologicalHistory = questionnaire.otherOncologicalHistory();
        assertEquals(1, otherOncologicalHistory.size());
        assertTrue(otherOncologicalHistory.contains("surgery"));

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("diabetes"));

        assertNull(questionnaire.molecularTests());

        assertEquals(TumorStage.III, questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableDisease());
        assertNull(questionnaire.hasBrainLesions());
        assertNull(questionnaire.hasActiveBrainLesions());
        assertNull(questionnaire.hasCnsLesions());
        assertNull(questionnaire.hasActiveCnsLesions());
        assertFalse(questionnaire.hasBoneLesions());
        assertFalse(questionnaire.hasLiverLesions());

        List<String> otherLesions = questionnaire.otherLesions();
        assertEquals(1, otherLesions.size());
        assertTrue(otherLesions.contains("pulmonal"));

        assertEquals(0, (int) questionnaire.whoStatus());
        assertTrue(questionnaire.unresolvedToxicities().isEmpty());

        InfectionStatus infectionStatus = questionnaire.infectionStatus();
        assertNotNull(infectionStatus);
        assertFalse(infectionStatus.hasActiveInfection());

        ECG ecg = questionnaire.ecg();
        assertNotNull(ecg);
        assertTrue(ecg.hasSigAberrationLatestECG());
        assertEquals("Sinus", ecg.aberrationDescription());

        List<String> complications = questionnaire.complications();
        assertEquals(1, complications.size());
        assertTrue(complications.contains("nausea"));
    }

    @Test
    public void canExtractFromQuestionnaireV1_0() {
        QuestionnaireEntry entry = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0());

        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);

        assertEquals(LocalDate.of(2020, 8, 28), questionnaire.date());
        assertEquals("lung", questionnaire.tumorLocation());
        assertEquals("small-cell carcinoma", questionnaire.tumorType());
        assertEquals("Liver", questionnaire.biopsyLocation());

        List<String> treatmentHistory = questionnaire.treatmentHistoryCurrentTumor();
        assertEquals(1, treatmentHistory.size());
        assertTrue(treatmentHistory.contains("capecitabine JAN 2020- JUL 2021"));

        List<String> otherOncologicalHistory = questionnaire.otherOncologicalHistory();
        assertEquals(1, otherOncologicalHistory.size());
        assertTrue(otherOncologicalHistory.contains("surgery JUN 2021"));

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("NO"));

        assertNull(questionnaire.molecularTests());

        assertEquals(TumorStage.IV, questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableDisease());
        assertNull(questionnaire.hasBrainLesions());
        assertNull(questionnaire.hasActiveBrainLesions());
        assertFalse(questionnaire.hasCnsLesions());
        assertFalse(questionnaire.hasActiveCnsLesions());
        assertFalse(questionnaire.hasBoneLesions());
        assertFalse(questionnaire.hasLiverLesions());

        List<String> otherLesions = questionnaire.otherLesions();
        assertEquals(3, otherLesions.size());
        assertTrue(otherLesions.contains("peritoneal"));
        assertTrue(otherLesions.contains("lymph nodes"));
        assertTrue(otherLesions.contains("lung"));

        assertEquals(1, (int) questionnaire.whoStatus());

        List<String> unresolvedToxicities = questionnaire.unresolvedToxicities();
        assertEquals(1, unresolvedToxicities.size());
        assertTrue(unresolvedToxicities.contains("NA"));

        InfectionStatus infectionStatus = questionnaire.infectionStatus();
        assertNotNull(infectionStatus);
        assertFalse(infectionStatus.hasActiveInfection());

        ECG ecg = questionnaire.ecg();
        assertNotNull(ecg);
        assertFalse(ecg.hasSigAberrationLatestECG());
        assertEquals("NA", ecg.aberrationDescription());

        List<String> complications = questionnaire.complications();
        assertEquals(1, complications.size());
        assertTrue(complications.contains("ascites"));
    }

    @Test
    public void canExtractFromQuestionnaireV0_2() {
        QuestionnaireEntry entry = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0_2());

        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);

        assertEquals(LocalDate.of(2020, 8, 28), questionnaire.date());
        assertEquals("cholangio", questionnaire.tumorLocation());
        assertEquals("carcinoma", questionnaire.tumorType());
        assertEquals("liver", questionnaire.biopsyLocation());

        List<String> treatmentHistory = questionnaire.treatmentHistoryCurrentTumor();
        assertEquals(1, treatmentHistory.size());
        assertTrue(treatmentHistory.contains("capecitabine"));

        List<String> otherOncologicalHistory = questionnaire.otherOncologicalHistory();
        assertEquals(1, otherOncologicalHistory.size());
        assertTrue(otherOncologicalHistory.contains("radiotherapy"));

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("NA"));

        assertNull(questionnaire.molecularTests());

        assertEquals(TumorStage.IV, questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableDisease());
        assertNull(questionnaire.hasBrainLesions());
        assertNull(questionnaire.hasActiveBrainLesions());
        assertNull(questionnaire.hasCnsLesions());
        assertNull(questionnaire.hasActiveCnsLesions());
        assertFalse(questionnaire.hasBoneLesions());
        assertFalse(questionnaire.hasLiverLesions());
        assertNull(questionnaire.otherLesions());

        assertEquals(2, (int) questionnaire.whoStatus());

        assertTrue(questionnaire.unresolvedToxicities().isEmpty());

        InfectionStatus infectionStatus = questionnaire.infectionStatus();
        assertNotNull(infectionStatus);
        assertFalse(infectionStatus.hasActiveInfection());

        assertNull(questionnaire.ecg());

        List<String> complications = questionnaire.complications();
        assertEquals(1, complications.size());
        assertTrue(complications.contains("pleural effusion"));
    }

    @Test
    public void canExtractFromQuestionnaireV0_1() {
        QuestionnaireEntry entry = entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1());

        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry);

        assertEquals(LocalDate.of(2020, 8, 28), questionnaire.date());
        assertEquals("Cholangiocarcinoom (lever, lymph retroperitoneaal)", questionnaire.tumorLocation());
        assertEquals("Unknown", questionnaire.tumorType());
        assertNull(questionnaire.biopsyLocation());
        assertNull(questionnaire.treatmentHistoryCurrentTumor());
        assertNull(questionnaire.otherOncologicalHistory());

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("Diabetes Mellitus type 2"));

        assertNull(questionnaire.molecularTests());

        assertNull(questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableDisease());
        assertNull(questionnaire.hasBrainLesions());
        assertNull(questionnaire.hasActiveBrainLesions());
        assertNull(questionnaire.hasCnsLesions());
        assertNull(questionnaire.hasActiveCnsLesions());
        assertTrue(questionnaire.hasBoneLesions());
        assertTrue(questionnaire.hasLiverLesions());

        List<String> otherLesions = questionnaire.otherLesions();
        assertEquals(2, otherLesions.size());
        assertTrue(otherLesions.contains("lever"));
        assertTrue(otherLesions.contains("lymph retroperitoneaal"));

        assertEquals(1, (int) questionnaire.whoStatus());

        List<String> unresolvedToxicities = questionnaire.unresolvedToxicities();
        assertEquals(1, unresolvedToxicities.size());
        assertTrue(unresolvedToxicities.contains("Neuropathy GR3"));

        InfectionStatus infectionStatus = questionnaire.infectionStatus();
        assertNotNull(infectionStatus);
        assertFalse(infectionStatus.hasActiveInfection());

        ECG ecg = questionnaire.ecg();
        assertNotNull(ecg);
        assertFalse(ecg.hasSigAberrationLatestECG());
        assertEquals("No", ecg.aberrationDescription());

        assertTrue(questionnaire.complications().isEmpty());
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