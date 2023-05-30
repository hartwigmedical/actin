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
    public void shouldBeAbleToDetermineThatQuestionnaireEntryIsAQuestionnaire() {
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
    public void shouldBeAbleToHandleMissingGENAYASubjectNumberFromQuestionnaire() {
        QuestionnaireEntry entryWithMissingSubjectNumber =
                entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_6().replace("GENAYA subjectno: GAYA-01-02-9999", ""));

        Questionnaire questionnaire = QuestionnaireExtraction.extract(entryWithMissingSubjectNumber);

        assertNull(questionnaire.genayaSubjectNumber());
    }

    @Test
    public void shouldBeAbleToExtractDataFromQuestionnaireV1_6() {
        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_6()));

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

        List<String> secondaryPrimaries = questionnaire.secondaryPrimaries();
        assertEquals(1, secondaryPrimaries.size());
        assertTrue(secondaryPrimaries.contains("sarcoma | Feb 2020"));

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("diabetes"));

        assertEquals(TumorStage.IV, questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableDisease());
        assertTrue(questionnaire.hasBrainLesions());
        assertTrue(questionnaire.hasActiveBrainLesions());
        assertNull(questionnaire.hasCnsLesions());
        assertNull(questionnaire.hasActiveCnsLesions());
        assertFalse(questionnaire.hasBoneLesions());
        assertFalse(questionnaire.hasLiverLesions());

        List<String> otherLesions = questionnaire.otherLesions();
        assertEquals(2, otherLesions.size());
        assertTrue(otherLesions.contains("pulmonal"));
        assertTrue(otherLesions.contains("abdominal"));

        List<String> ihcTestResults = questionnaire.ihcTestResults();
        assertEquals(1, ihcTestResults.size());
        assertTrue(ihcTestResults.contains("ERBB2 3+"));

        List<String> pdl1TestResults = questionnaire.pdl1TestResults();
        assertEquals(1, pdl1TestResults.size());
        assertTrue(pdl1TestResults.contains("Positive"));

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

        assertEquals("GAYA-01-02-9999", questionnaire.genayaSubjectNumber());
    }

    @Test
    public void shouldBeAbleToExtractDataFromQuestionnaireV1_5() {
        assertExtractionForQuestionnaireV1_5(TestQuestionnaireFactory.createTestQuestionnaireValueV1_5());
    }

    @Test
    public void shouldBeAbleToExtractDataFromAlternateQuestionnaireV1_5() {
        String rawQuestionnaire = TestQuestionnaireFactory.createTestQuestionnaireValueV1_5()
                .replace("- IHC test", "-IHC test")
                .replace("- PD L1 test", "-PD L1 test");

        assertExtractionForQuestionnaireV1_5(rawQuestionnaire);
    }

    private void assertExtractionForQuestionnaireV1_5(@NotNull String rawQuestionnaire) {
        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry(rawQuestionnaire));

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

        List<String> secondaryPrimaries = questionnaire.secondaryPrimaries();
        assertEquals(1, secondaryPrimaries.size());
        assertTrue(secondaryPrimaries.contains("sarcoma | Feb 2020"));

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("diabetes"));

        assertEquals(TumorStage.IV, questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableDisease());
        assertTrue(questionnaire.hasBrainLesions());
        assertTrue(questionnaire.hasActiveBrainLesions());
        assertNull(questionnaire.hasCnsLesions());
        assertNull(questionnaire.hasActiveCnsLesions());
        assertFalse(questionnaire.hasBoneLesions());
        assertFalse(questionnaire.hasLiverLesions());

        List<String> otherLesions = questionnaire.otherLesions();
        assertEquals(2, otherLesions.size());
        assertTrue(otherLesions.contains("pulmonal"));
        assertTrue(otherLesions.contains("abdominal"));

        List<String> ihcTestResults = questionnaire.ihcTestResults();
        assertEquals(1, ihcTestResults.size());
        assertTrue(ihcTestResults.contains("ERBB2 3+"));

        List<String> pdl1TestResults = questionnaire.pdl1TestResults();
        assertEquals(1, pdl1TestResults.size());
        assertTrue(pdl1TestResults.contains("Positive"));

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

        assertNull(questionnaire.genayaSubjectNumber());
    }

    @Test
    public void shouldBeAbleToExtractDataFromQuestionnaireV1_4() {
        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_4()));

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

        assertNull(questionnaire.secondaryPrimaries());

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("diabetes"));

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

        List<String> ihcTestResults = questionnaire.ihcTestResults();
        assertEquals(1, ihcTestResults.size());
        assertTrue(ihcTestResults.contains("IHC ERBB2 3+"));
        assertNull(questionnaire.pdl1TestResults());

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

        assertNull(questionnaire.genayaSubjectNumber());
    }

    @Test
    public void shouldBeAbleToExtractDataFromQuestionnaireV1_3() {
        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_3()));

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

        assertNull(questionnaire.secondaryPrimaries());

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("diabetes"));

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

        assertNull(questionnaire.ihcTestResults());
        assertNull(questionnaire.pdl1TestResults());

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

        assertNull(questionnaire.genayaSubjectNumber());
    }

    @Test
    public void shouldBeAbleToExtractDataFromQuestionnaireV1_2() {
        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_2()));

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

        assertNull(questionnaire.secondaryPrimaries());

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("diabetes"));

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

        assertNull(questionnaire.ihcTestResults());
        assertNull(questionnaire.pdl1TestResults());

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

        assertNull(questionnaire.genayaSubjectNumber());
    }

    @Test
    public void shouldBeAbleToExtractDataFromQuestionnaireV1_1() {
        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_1()));

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

        assertNull(questionnaire.secondaryPrimaries());

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("diabetes"));

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

        assertNull(questionnaire.ihcTestResults());
        assertNull(questionnaire.pdl1TestResults());

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

        assertNull(questionnaire.genayaSubjectNumber());
    }

    @Test
    public void shouldBeAbleToExtractDataFromQuestionnaireV1_0() {
        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0()));

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

        assertNull(questionnaire.secondaryPrimaries());

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("NO"));

        assertEquals(TumorStage.IV, questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableDisease());
        assertNull(questionnaire.hasBrainLesions());
        assertNull(questionnaire.hasActiveBrainLesions());
        assertFalse(questionnaire.hasCnsLesions());
        assertNull(questionnaire.hasActiveCnsLesions());
        assertFalse(questionnaire.hasBoneLesions());
        assertFalse(questionnaire.hasLiverLesions());

        List<String> otherLesions = questionnaire.otherLesions();
        assertEquals(3, otherLesions.size());
        assertTrue(otherLesions.contains("peritoneal"));
        assertTrue(otherLesions.contains("lymph nodes"));
        assertTrue(otherLesions.contains("lung"));

        assertNull(questionnaire.ihcTestResults());
        assertNull(questionnaire.pdl1TestResults());

        assertEquals(1, (int) questionnaire.whoStatus());

        List<String> unresolvedToxicities = questionnaire.unresolvedToxicities();
        assertEquals(1, unresolvedToxicities.size());
        assertTrue(unresolvedToxicities.contains("NA"));

        InfectionStatus infectionStatus = questionnaire.infectionStatus();
        assertNotNull(infectionStatus);
        assertFalse(infectionStatus.hasActiveInfection());

        assertNull(questionnaire.ecg());

        List<String> complications = questionnaire.complications();
        assertEquals(1, complications.size());
        assertTrue(complications.contains("ascites"));

        assertNull(questionnaire.genayaSubjectNumber());
    }

    @Test
    public void shouldBeAbleToExtractDataFromQuestionnaireV0_2() {
        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0_2()));

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

        assertNull(questionnaire.secondaryPrimaries());

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("NA"));

        assertEquals(TumorStage.IV, questionnaire.stage());
        assertTrue(questionnaire.hasMeasurableDisease());
        assertNull(questionnaire.hasBrainLesions());
        assertNull(questionnaire.hasActiveBrainLesions());
        assertNull(questionnaire.hasCnsLesions());
        assertNull(questionnaire.hasActiveCnsLesions());
        assertFalse(questionnaire.hasBoneLesions());
        assertFalse(questionnaire.hasLiverLesions());
        assertNull(questionnaire.otherLesions());

        assertNull(questionnaire.ihcTestResults());
        assertNull(questionnaire.pdl1TestResults());

        assertEquals(2, (int) questionnaire.whoStatus());

        assertTrue(questionnaire.unresolvedToxicities().isEmpty());

        InfectionStatus infectionStatus = questionnaire.infectionStatus();
        assertNotNull(infectionStatus);
        assertFalse(infectionStatus.hasActiveInfection());

        assertNull(questionnaire.ecg());

        List<String> complications = questionnaire.complications();
        assertEquals(1, complications.size());
        assertTrue(complications.contains("pleural effusion"));

        assertNull(questionnaire.genayaSubjectNumber());
    }

    @Test
    public void shouldBeAbleToExtractDataFromQuestionnaireV0_1() {
        Questionnaire questionnaire = QuestionnaireExtraction.extract(entry(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1()));

        assertEquals(LocalDate.of(2020, 8, 28), questionnaire.date());
        assertEquals("Cholangiocarcinoom (lever, lymph retroperitoneaal)", questionnaire.tumorLocation());
        assertEquals("Unknown", questionnaire.tumorType());
        assertNull(questionnaire.biopsyLocation());
        assertNull(questionnaire.treatmentHistoryCurrentTumor());
        assertNull(questionnaire.otherOncologicalHistory());
        assertNull(questionnaire.secondaryPrimaries());

        List<String> nonOncologicalHistory = questionnaire.nonOncologicalHistory();
        assertEquals(1, nonOncologicalHistory.size());
        assertTrue(nonOncologicalHistory.contains("Diabetes Mellitus type 2"));

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

        assertNull(questionnaire.ihcTestResults());
        assertNull(questionnaire.pdl1TestResults());

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

        assertNull(questionnaire.genayaSubjectNumber());
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
                .text(questionnaire)
                .build();
    }
}