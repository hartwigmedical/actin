package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireExtraction.Companion.isActualQuestionnaire
import com.hartwig.actin.clinical.feed.questionnaire.TestQuestionnaireFactory.entryWithText
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.time.LocalDate

class QuestionnaireExtractionTest {
    @Test
    fun shouldBeAbleToDetermineThatQuestionnaireEntryIsAQuestionnaire() {
        assertTrue(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_7())))
        assertTrue(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_6())))
        assertTrue(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_5())))
        assertTrue(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_4())))
        assertTrue(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_3())))
        assertTrue(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_2())))
        assertTrue(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_1())))
        assertTrue(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0())))
        assertTrue(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV0_2())))
        assertTrue(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1())))
        assertFalse(isActualQuestionnaire(entryWithText("Does not exist")))
    }

    @Test
    fun shouldBeAbleToHandleMissingGENAYASubjectNumberFromQuestionnaire() {
        val entryWithMissingSubjectNumber =
            entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_6().replace("GENAYA subjectno: GAYA-01-02-9999", ""))
        val questionnaire = extraction().extract(entryWithMissingSubjectNumber)
        assertNull(questionnaire!!.genayaSubjectNumber)
    }

    @Test
    fun shouldBeAbleToExtractDataFromQuestionnaireV1_7() {
        val questionnaire = extraction().extract(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_7()))
        assertEquals(LocalDate.of(2020, 8, 28), questionnaire!!.date)
        assertEquals("ovary", questionnaire.tumorLocation)
        assertEquals("serous", questionnaire.tumorType)
        assertEquals("lymph node", questionnaire.biopsyLocation)
        val treatmentHistory = questionnaire.treatmentHistoryCurrentTumor
        assertEquals(2, treatmentHistory!!.size.toLong())
        assertTrue(treatmentHistory.contains("cisplatin"))
        assertTrue(treatmentHistory.contains("nivolumab"))
        val otherOncologicalHistory = questionnaire.otherOncologicalHistory
        assertEquals(1, otherOncologicalHistory!!.size.toLong())
        assertTrue(otherOncologicalHistory.contains("surgery"))
        val secondaryPrimaries = questionnaire.secondaryPrimaries
        assertEquals(1, secondaryPrimaries!!.size.toLong())
        assertTrue(secondaryPrimaries.contains("sarcoma | Feb 2020"))
        val nonOncologicalHistory = questionnaire.nonOncologicalHistory
        assertEquals(1, nonOncologicalHistory!!.size.toLong())
        assertTrue(nonOncologicalHistory.contains("diabetes"))
        assertEquals(TumorStage.IV, questionnaire.stage)
        assertTrue(questionnaire.hasMeasurableDisease!!)
        assertTrue(questionnaire.hasBrainLesions!!)
        assertTrue(questionnaire.hasActiveBrainLesions!!)
        assertNull(questionnaire.hasCnsLesions)
        assertNull(questionnaire.hasActiveCnsLesions)
        assertFalse(questionnaire.hasBoneLesions!!)
        assertFalse(questionnaire.hasLiverLesions!!)
        val otherLesions = questionnaire.otherLesions
        assertEquals(2, otherLesions!!.size.toLong())
        assertTrue(otherLesions.contains("pulmonal"))
        assertTrue(otherLesions.contains("abdominal"))
        val ihcTestResults = questionnaire.ihcTestResults
        assertEquals(1, ihcTestResults!!.size.toLong())
        assertTrue(ihcTestResults.contains("ERBB2 3+"))
        val pdl1TestResults = questionnaire.pdl1TestResults
        assertEquals(1, pdl1TestResults!!.size.toLong())
        assertTrue(pdl1TestResults.contains("Positive"))
        assertEquals(0, (questionnaire.whoStatus as Int).toLong())
        val unresolvedToxicities = questionnaire.unresolvedToxicities
        assertEquals(1, unresolvedToxicities!!.size.toLong())
        assertTrue(unresolvedToxicities.contains("toxic"))
        val infectionStatus = questionnaire.infectionStatus
        assertNotNull(infectionStatus)
        assertFalse(infectionStatus!!.hasActiveInfection())
        val ecg = questionnaire.ecg
        assertNotNull(ecg)
        assertTrue(ecg!!.hasSigAberrationLatestECG())
        assertEquals("Sinus", ecg.aberrationDescription())
        val complications = questionnaire.complications
        assertEquals(1, complications!!.size.toLong())
        assertTrue(complications.contains("vomit"))
        assertEquals("GAYA-01-02-9999", questionnaire.genayaSubjectNumber)
    }

    @Test
    fun shouldBeAbleToExtractDataFromQuestionnaireV1_6() {
        val questionnaire = extraction().extract(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_6()))
        assertEquals(LocalDate.of(2020, 8, 28), questionnaire!!.date)
        assertEquals("ovary", questionnaire.tumorLocation)
        assertEquals("serous", questionnaire.tumorType)
        assertEquals("lymph node", questionnaire.biopsyLocation)
        val treatmentHistory = questionnaire.treatmentHistoryCurrentTumor
        assertEquals(2, treatmentHistory!!.size.toLong())
        assertTrue(treatmentHistory.contains("cisplatin"))
        assertTrue(treatmentHistory.contains("nivolumab"))
        val otherOncologicalHistory = questionnaire.otherOncologicalHistory
        assertEquals(1, otherOncologicalHistory!!.size.toLong())
        assertTrue(otherOncologicalHistory.contains("surgery"))
        val secondaryPrimaries = questionnaire.secondaryPrimaries
        assertEquals(1, secondaryPrimaries!!.size.toLong())
        assertTrue(secondaryPrimaries.contains("sarcoma | Feb 2020"))
        val nonOncologicalHistory = questionnaire.nonOncologicalHistory
        assertEquals(1, nonOncologicalHistory!!.size.toLong())
        assertTrue(nonOncologicalHistory.contains("diabetes"))
        assertEquals(TumorStage.IV, questionnaire.stage)
        assertTrue(questionnaire.hasMeasurableDisease!!)
        assertTrue(questionnaire.hasBrainLesions!!)
        assertTrue(questionnaire.hasActiveBrainLesions!!)
        assertNull(questionnaire.hasCnsLesions)
        assertNull(questionnaire.hasActiveCnsLesions)
        assertFalse(questionnaire.hasBoneLesions!!)
        assertFalse(questionnaire.hasLiverLesions!!)
        val otherLesions = questionnaire.otherLesions
        assertEquals(2, otherLesions!!.size.toLong())
        assertTrue(otherLesions.contains("pulmonal"))
        assertTrue(otherLesions.contains("abdominal"))
        val ihcTestResults = questionnaire.ihcTestResults
        assertEquals(1, ihcTestResults!!.size.toLong())
        assertTrue(ihcTestResults.contains("ERBB2 3+"))
        val pdl1TestResults = questionnaire.pdl1TestResults
        assertEquals(1, pdl1TestResults!!.size.toLong())
        assertTrue(pdl1TestResults.contains("Positive"))
        assertEquals(0, (questionnaire.whoStatus as Int).toLong())
        val unresolvedToxicities = questionnaire.unresolvedToxicities
        assertEquals(1, unresolvedToxicities!!.size.toLong())
        assertTrue(unresolvedToxicities.contains("toxic"))
        val infectionStatus = questionnaire.infectionStatus
        assertNotNull(infectionStatus)
        assertFalse(infectionStatus!!.hasActiveInfection())
        val ecg = questionnaire.ecg
        assertNotNull(ecg)
        assertTrue(ecg!!.hasSigAberrationLatestECG())
        assertEquals("Sinus", ecg.aberrationDescription())
        val complications = questionnaire.complications
        assertEquals(1, complications!!.size.toLong())
        assertTrue(complications.contains("vomit"))
        assertEquals("GAYA-01-02-9999", questionnaire.genayaSubjectNumber)
    }

    @Test
    fun shouldBeAbleToExtractDataFromQuestionnaireV1_5() {
        assertExtractionForQuestionnaireV1_5(TestQuestionnaireFactory.createTestQuestionnaireValueV1_5())
    }

    @Test
    fun shouldBeAbleToExtractDataFromAlternateQuestionnaireV1_5() {
        val rawQuestionnaire = TestQuestionnaireFactory.createTestQuestionnaireValueV1_5()
            .replace("- IHC test", "-IHC test")
            .replace("- PD L1 test", "-PD L1 test")
        assertExtractionForQuestionnaireV1_5(rawQuestionnaire)
    }

    private fun assertExtractionForQuestionnaireV1_5(rawQuestionnaire: String) {
        val questionnaire = extraction().extract(entryWithText(rawQuestionnaire))
        assertEquals(LocalDate.of(2020, 8, 28), questionnaire!!.date)
        assertEquals("ovary", questionnaire.tumorLocation)
        assertEquals("serous", questionnaire.tumorType)
        assertEquals("lymph node", questionnaire.biopsyLocation)
        val treatmentHistory = questionnaire.treatmentHistoryCurrentTumor
        assertEquals(2, treatmentHistory!!.size.toLong())
        assertTrue(treatmentHistory.contains("cisplatin"))
        assertTrue(treatmentHistory.contains("nivolumab"))
        val otherOncologicalHistory = questionnaire.otherOncologicalHistory
        assertEquals(1, otherOncologicalHistory!!.size.toLong())
        assertTrue(otherOncologicalHistory.contains("surgery"))
        val secondaryPrimaries = questionnaire.secondaryPrimaries
        assertEquals(1, secondaryPrimaries!!.size.toLong())
        assertTrue(secondaryPrimaries.contains("sarcoma | Feb 2020"))
        val nonOncologicalHistory = questionnaire.nonOncologicalHistory
        assertEquals(1, nonOncologicalHistory!!.size.toLong())
        assertTrue(nonOncologicalHistory.contains("diabetes"))
        assertEquals(TumorStage.IV, questionnaire.stage)
        assertTrue(questionnaire.hasMeasurableDisease!!)
        assertTrue(questionnaire.hasBrainLesions!!)
        assertTrue(questionnaire.hasActiveBrainLesions!!)
        assertNull(questionnaire.hasCnsLesions)
        assertNull(questionnaire.hasActiveCnsLesions)
        assertFalse(questionnaire.hasBoneLesions!!)
        assertFalse(questionnaire.hasLiverLesions!!)
        val otherLesions = questionnaire.otherLesions
        assertEquals(2, otherLesions!!.size.toLong())
        assertTrue(otherLesions.contains("pulmonal"))
        assertTrue(otherLesions.contains("abdominal"))
        val ihcTestResults = questionnaire.ihcTestResults
        assertEquals(1, ihcTestResults!!.size.toLong())
        assertTrue(ihcTestResults.contains("ERBB2 3+"))
        val pdl1TestResults = questionnaire.pdl1TestResults
        assertEquals(1, pdl1TestResults!!.size.toLong())
        assertTrue(pdl1TestResults.contains("Positive"))
        assertEquals(0, (questionnaire.whoStatus as Int).toLong())
        val unresolvedToxicities = questionnaire.unresolvedToxicities
        assertEquals(1, unresolvedToxicities!!.size.toLong())
        assertTrue(unresolvedToxicities.contains("toxic"))
        val infectionStatus = questionnaire.infectionStatus
        assertNotNull(infectionStatus)
        assertFalse(infectionStatus!!.hasActiveInfection())
        val ecg = questionnaire.ecg
        assertNotNull(ecg)
        assertTrue(ecg!!.hasSigAberrationLatestECG())
        assertEquals("Sinus", ecg.aberrationDescription())
        val complications = questionnaire.complications
        assertEquals(1, complications!!.size.toLong())
        assertTrue(complications.contains("vomit"))
        assertNull(questionnaire.genayaSubjectNumber)
    }

    @Test
    fun shouldBeAbleToExtractDataFromQuestionnaireV1_4() {
        val questionnaire = extraction().extract(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_4()))
        assertEquals(LocalDate.of(2020, 8, 28), questionnaire!!.date)
        assertEquals("ovary", questionnaire.tumorLocation)
        assertEquals("serous", questionnaire.tumorType)
        assertEquals("Lymph node", questionnaire.biopsyLocation)
        val treatmentHistory = questionnaire.treatmentHistoryCurrentTumor
        assertEquals(2, treatmentHistory!!.size.toLong())
        assertTrue(treatmentHistory.contains("cisplatin"))
        assertTrue(treatmentHistory.contains("nivolumab"))
        val otherOncologicalHistory = questionnaire.otherOncologicalHistory
        assertEquals(1, otherOncologicalHistory!!.size.toLong())
        assertTrue(otherOncologicalHistory.contains("surgery"))
        assertNull(questionnaire.secondaryPrimaries)
        val nonOncologicalHistory = questionnaire.nonOncologicalHistory
        assertEquals(1, nonOncologicalHistory!!.size.toLong())
        assertTrue(nonOncologicalHistory.contains("diabetes"))
        assertEquals(TumorStage.III, questionnaire.stage)
        assertTrue(questionnaire.hasMeasurableDisease!!)
        assertNull(questionnaire.hasBrainLesions)
        assertNull(questionnaire.hasActiveBrainLesions)
        assertNull(questionnaire.hasCnsLesions)
        assertNull(questionnaire.hasActiveCnsLesions)
        assertFalse(questionnaire.hasBoneLesions!!)
        assertFalse(questionnaire.hasLiverLesions!!)
        val otherLesions = questionnaire.otherLesions
        assertEquals(1, otherLesions!!.size.toLong())
        assertTrue(otherLesions.contains("pulmonal"))
        val ihcTestResults = questionnaire.ihcTestResults
        assertEquals(1, ihcTestResults!!.size.toLong())
        assertTrue(ihcTestResults.contains("IHC ERBB2 3+"))
        assertNull(questionnaire.pdl1TestResults)
        assertEquals(0, (questionnaire.whoStatus as Int).toLong())
        assertNull(questionnaire.unresolvedToxicities)
        val infectionStatus = questionnaire.infectionStatus
        assertNotNull(infectionStatus)
        assertFalse(infectionStatus!!.hasActiveInfection())
        val ecg = questionnaire.ecg
        assertNotNull(ecg)
        assertTrue(ecg!!.hasSigAberrationLatestECG())
        assertEquals("Sinus", ecg.aberrationDescription())
        val complications = questionnaire.complications
        assertEquals(1, complications!!.size.toLong())
        assertTrue(complications.contains("nausea"))
        assertNull(questionnaire.genayaSubjectNumber)
    }

    @Test
    fun shouldBeAbleToExtractDataFromQuestionnaireV1_3() {
        val questionnaire = extraction().extract(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_3()))
        assertEquals(LocalDate.of(2020, 8, 28), questionnaire!!.date)
        assertEquals("ovary", questionnaire.tumorLocation)
        assertEquals("serous", questionnaire.tumorType)
        assertEquals("Lymph node", questionnaire.biopsyLocation)
        val treatmentHistory = questionnaire.treatmentHistoryCurrentTumor
        assertEquals(2, treatmentHistory!!.size.toLong())
        assertTrue(treatmentHistory.contains("cisplatin"))
        assertTrue(treatmentHistory.contains("nivolumab"))
        val otherOncologicalHistory = questionnaire.otherOncologicalHistory
        assertEquals(1, otherOncologicalHistory!!.size.toLong())
        assertTrue(otherOncologicalHistory.contains("surgery"))
        assertNull(questionnaire.secondaryPrimaries)
        val nonOncologicalHistory = questionnaire.nonOncologicalHistory
        assertEquals(1, nonOncologicalHistory!!.size.toLong())
        assertTrue(nonOncologicalHistory.contains("diabetes"))
        assertEquals(TumorStage.III, questionnaire.stage)
        assertTrue(questionnaire.hasMeasurableDisease!!)
        assertNull(questionnaire.hasBrainLesions)
        assertNull(questionnaire.hasActiveBrainLesions)
        assertNull(questionnaire.hasCnsLesions)
        assertNull(questionnaire.hasActiveCnsLesions)
        assertFalse(questionnaire.hasBoneLesions!!)
        assertFalse(questionnaire.hasLiverLesions!!)
        val otherLesions = questionnaire.otherLesions
        assertEquals(1, otherLesions!!.size.toLong())
        assertTrue(otherLesions.contains("pulmonal"))
        assertNull(questionnaire.ihcTestResults)
        assertNull(questionnaire.pdl1TestResults)
        assertEquals(0, (questionnaire.whoStatus as Int).toLong())
        assertNull(questionnaire.unresolvedToxicities)
        val infectionStatus = questionnaire.infectionStatus
        assertNotNull(infectionStatus)
        assertFalse(infectionStatus!!.hasActiveInfection())
        val ecg = questionnaire.ecg
        assertNotNull(ecg)
        assertTrue(ecg!!.hasSigAberrationLatestECG())
        assertEquals("Sinus", ecg.aberrationDescription())
        val complications = questionnaire.complications
        assertEquals(1, complications!!.size.toLong())
        assertTrue(complications.contains("nausea"))
        assertNull(questionnaire.genayaSubjectNumber)
    }

    @Test
    fun shouldBeAbleToExtractDataFromQuestionnaireV1_2() {
        val questionnaire = extraction().extract(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_2()))
        assertEquals(LocalDate.of(2020, 8, 28), questionnaire!!.date)
        assertEquals("ovary", questionnaire.tumorLocation)
        assertEquals("serous", questionnaire.tumorType)
        assertEquals("Lymph node", questionnaire.biopsyLocation)
        val treatmentHistory = questionnaire.treatmentHistoryCurrentTumor
        assertEquals(2, treatmentHistory!!.size.toLong())
        assertTrue(treatmentHistory.contains("cisplatin"))
        assertTrue(treatmentHistory.contains("nivolumab"))
        val otherOncologicalHistory = questionnaire.otherOncologicalHistory
        assertEquals(1, otherOncologicalHistory!!.size.toLong())
        assertTrue(otherOncologicalHistory.contains("surgery"))
        assertNull(questionnaire.secondaryPrimaries)
        val nonOncologicalHistory = questionnaire.nonOncologicalHistory
        assertEquals(1, nonOncologicalHistory!!.size.toLong())
        assertTrue(nonOncologicalHistory.contains("diabetes"))
        assertEquals(TumorStage.III, questionnaire.stage)
        assertTrue(questionnaire.hasMeasurableDisease!!)
        assertNull(questionnaire.hasBrainLesions)
        assertNull(questionnaire.hasActiveBrainLesions)
        assertNull(questionnaire.hasCnsLesions)
        assertNull(questionnaire.hasActiveCnsLesions)
        assertFalse(questionnaire.hasBoneLesions!!)
        assertFalse(questionnaire.hasLiverLesions!!)
        val otherLesions = questionnaire.otherLesions
        assertEquals(1, otherLesions!!.size.toLong())
        assertTrue(otherLesions.contains("pulmonal"))
        assertNull(questionnaire.ihcTestResults)
        assertNull(questionnaire.pdl1TestResults)
        assertEquals(0, (questionnaire.whoStatus as Int).toLong())
        assertNull(questionnaire.unresolvedToxicities)
        val infectionStatus = questionnaire.infectionStatus
        assertNotNull(infectionStatus)
        assertFalse(infectionStatus!!.hasActiveInfection())
        val ecg = questionnaire.ecg
        assertNotNull(ecg)
        assertTrue(ecg!!.hasSigAberrationLatestECG())
        assertEquals("Sinus", ecg.aberrationDescription())
        val complications = questionnaire.complications
        assertEquals(1, complications!!.size.toLong())
        assertTrue(complications.contains("nausea"))
        assertNull(questionnaire.genayaSubjectNumber)
    }

    @Test
    fun shouldBeAbleToExtractDataFromQuestionnaireV1_1() {
        val questionnaire = extraction().extract(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_1()))
        assertEquals(LocalDate.of(2020, 8, 28), questionnaire!!.date)
        assertEquals("ovary", questionnaire.tumorLocation)
        assertEquals("serous", questionnaire.tumorType)
        assertEquals("Lymph node", questionnaire.biopsyLocation)
        val treatmentHistory = questionnaire.treatmentHistoryCurrentTumor
        assertEquals(2, treatmentHistory!!.size.toLong())
        assertTrue(treatmentHistory.contains("cisplatin"))
        assertTrue(treatmentHistory.contains("nivolumab"))
        val otherOncologicalHistory = questionnaire.otherOncologicalHistory
        assertEquals(1, otherOncologicalHistory!!.size.toLong())
        assertTrue(otherOncologicalHistory.contains("surgery"))
        assertNull(questionnaire.secondaryPrimaries)
        val nonOncologicalHistory = questionnaire.nonOncologicalHistory
        assertEquals(1, nonOncologicalHistory!!.size.toLong())
        assertTrue(nonOncologicalHistory.contains("diabetes"))
        assertEquals(TumorStage.III, questionnaire.stage)
        assertTrue(questionnaire.hasMeasurableDisease!!)
        assertNull(questionnaire.hasBrainLesions)
        assertNull(questionnaire.hasActiveBrainLesions)
        assertNull(questionnaire.hasCnsLesions)
        assertNull(questionnaire.hasActiveCnsLesions)
        assertFalse(questionnaire.hasBoneLesions!!)
        assertFalse(questionnaire.hasLiverLesions!!)
        val otherLesions = questionnaire.otherLesions
        assertEquals(1, otherLesions!!.size.toLong())
        assertTrue(otherLesions.contains("pulmonal"))
        assertNull(questionnaire.ihcTestResults)
        assertNull(questionnaire.pdl1TestResults)
        assertEquals(0, (questionnaire.whoStatus as Int).toLong())
        assertNull(questionnaire.unresolvedToxicities)
        val infectionStatus = questionnaire.infectionStatus
        assertNotNull(infectionStatus)
        assertFalse(infectionStatus!!.hasActiveInfection())
        val ecg = questionnaire.ecg
        assertNotNull(ecg)
        assertTrue(ecg!!.hasSigAberrationLatestECG())
        assertEquals("Sinus", ecg.aberrationDescription())
        val complications = questionnaire.complications
        assertEquals(1, complications!!.size.toLong())
        assertTrue(complications.contains("nausea"))
        assertNull(questionnaire.genayaSubjectNumber)
    }

    @Test
    fun shouldBeAbleToExtractDataFromQuestionnaireV1_0() {
        val questionnaire = extraction().extract(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0()))
        assertEquals(LocalDate.of(2020, 8, 28), questionnaire!!.date)
        assertEquals("lung", questionnaire.tumorLocation)
        assertEquals("small-cell carcinoma", questionnaire.tumorType)
        assertEquals("Liver", questionnaire.biopsyLocation)
        val treatmentHistory = questionnaire.treatmentHistoryCurrentTumor
        assertEquals(1, treatmentHistory!!.size.toLong())
        assertTrue(treatmentHistory.contains("capecitabine JAN 2020- JUL 2021"))
        val otherOncologicalHistory = questionnaire.otherOncologicalHistory
        assertEquals(1, otherOncologicalHistory!!.size.toLong())
        assertTrue(otherOncologicalHistory.contains("surgery JUN 2021"))
        assertNull(questionnaire.secondaryPrimaries)
        val nonOncologicalHistory = questionnaire.nonOncologicalHistory
        assertEquals(1, nonOncologicalHistory!!.size.toLong())
        assertTrue(nonOncologicalHistory.contains("NO"))
        assertEquals(TumorStage.IV, questionnaire.stage)
        assertTrue(questionnaire.hasMeasurableDisease!!)
        assertNull(questionnaire.hasBrainLesions)
        assertNull(questionnaire.hasActiveBrainLesions)
        assertFalse(questionnaire.hasCnsLesions!!)
        assertNull(questionnaire.hasActiveCnsLesions)
        assertFalse(questionnaire.hasBoneLesions!!)
        assertFalse(questionnaire.hasLiverLesions!!)
        val otherLesions = questionnaire.otherLesions
        assertEquals(3, otherLesions!!.size.toLong())
        assertTrue(otherLesions.contains("peritoneal"))
        assertTrue(otherLesions.contains("lymph nodes"))
        assertTrue(otherLesions.contains("lung"))
        assertNull(questionnaire.ihcTestResults)
        assertNull(questionnaire.pdl1TestResults)
        assertEquals(1, (questionnaire.whoStatus as Int).toLong())
        val unresolvedToxicities = questionnaire.unresolvedToxicities
        assertEquals(1, unresolvedToxicities!!.size.toLong())
        assertTrue(unresolvedToxicities.contains("NA"))
        val infectionStatus = questionnaire.infectionStatus
        assertNotNull(infectionStatus)
        assertFalse(infectionStatus!!.hasActiveInfection())
        assertNull(questionnaire.ecg)
        val complications = questionnaire.complications
        assertEquals(1, complications!!.size.toLong())
        assertTrue(complications.contains("ascites"))
        assertNull(questionnaire.genayaSubjectNumber)
    }

    @Test
    fun shouldBeAbleToExtractDataFromQuestionnaireV0_2() {
        val questionnaire = extraction().extract(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV0_2()))
        assertEquals(LocalDate.of(2020, 8, 28), questionnaire!!.date)
        assertEquals("cholangio", questionnaire.tumorLocation)
        assertEquals("carcinoma", questionnaire.tumorType)
        assertEquals("liver", questionnaire.biopsyLocation)
        val treatmentHistory = questionnaire.treatmentHistoryCurrentTumor
        assertEquals(1, treatmentHistory!!.size.toLong())
        assertTrue(treatmentHistory.contains("capecitabine"))
        val otherOncologicalHistory = questionnaire.otherOncologicalHistory
        assertEquals(1, otherOncologicalHistory!!.size.toLong())
        assertTrue(otherOncologicalHistory.contains("radiotherapy"))
        assertNull(questionnaire.secondaryPrimaries)
        val nonOncologicalHistory = questionnaire.nonOncologicalHistory
        assertEquals(1, nonOncologicalHistory!!.size.toLong())
        assertTrue(nonOncologicalHistory.contains("NA"))
        assertEquals(TumorStage.IV, questionnaire.stage)
        assertTrue(questionnaire.hasMeasurableDisease!!)
        assertNull(questionnaire.hasBrainLesions)
        assertNull(questionnaire.hasActiveBrainLesions)
        assertNull(questionnaire.hasCnsLesions)
        assertNull(questionnaire.hasActiveCnsLesions)
        assertFalse(questionnaire.hasBoneLesions!!)
        assertFalse(questionnaire.hasLiverLesions!!)
        assertNull(questionnaire.otherLesions)
        assertNull(questionnaire.ihcTestResults)
        assertNull(questionnaire.pdl1TestResults)
        assertEquals(2, (questionnaire.whoStatus as Int).toLong())
        assertNull(questionnaire.unresolvedToxicities)
        val infectionStatus = questionnaire.infectionStatus
        assertNotNull(infectionStatus)
        assertFalse(infectionStatus!!.hasActiveInfection())
        assertNull(questionnaire.ecg)
        val complications = questionnaire.complications
        assertEquals(1, complications!!.size.toLong())
        assertTrue(complications.contains("pleural effusion"))
        assertNull(questionnaire.genayaSubjectNumber)
    }

    @Test
    fun shouldBeAbleToExtractDataFromQuestionnaireV0_1() {
        val questionnaire = extraction().extract(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1()))
        assertEquals(LocalDate.of(2020, 8, 28), questionnaire!!.date)
        assertEquals("Cholangiocarcinoom (lever, lymph retroperitoneaal)", questionnaire.tumorLocation)
        assertEquals("Unknown", questionnaire.tumorType)
        assertNull(questionnaire.biopsyLocation)
        assertNull(questionnaire.treatmentHistoryCurrentTumor)
        assertNull(questionnaire.otherOncologicalHistory)
        assertNull(questionnaire.secondaryPrimaries)
        val nonOncologicalHistory = questionnaire.nonOncologicalHistory
        assertEquals(1, nonOncologicalHistory!!.size.toLong())
        assertTrue(nonOncologicalHistory.contains("Diabetes Mellitus type 2"))
        assertNull(questionnaire.stage)
        assertTrue(questionnaire.hasMeasurableDisease!!)
        assertNull(questionnaire.hasBrainLesions)
        assertNull(questionnaire.hasActiveBrainLesions)
        assertNull(questionnaire.hasCnsLesions)
        assertNull(questionnaire.hasActiveCnsLesions)
        assertTrue(questionnaire.hasBoneLesions!!)
        assertTrue(questionnaire.hasLiverLesions!!)
        val otherLesions = questionnaire.otherLesions
        assertEquals(2, otherLesions!!.size.toLong())
        assertTrue(otherLesions.contains("lever"))
        assertTrue(otherLesions.contains("lymph retroperitoneaal"))
        assertNull(questionnaire.ihcTestResults)
        assertNull(questionnaire.pdl1TestResults)
        assertEquals(1, (questionnaire.whoStatus as Int).toLong())
        val unresolvedToxicities = questionnaire.unresolvedToxicities
        assertEquals(1, unresolvedToxicities!!.size.toLong())
        assertTrue(unresolvedToxicities.contains("Neuropathy GR3"))
        val infectionStatus = questionnaire.infectionStatus
        assertNotNull(infectionStatus)
        assertFalse(infectionStatus!!.hasActiveInfection())
        val ecg = questionnaire.ecg
        assertNotNull(ecg)
        assertFalse(ecg!!.hasSigAberrationLatestECG())
        assertEquals("No", ecg.aberrationDescription())
        assertNull(questionnaire.complications)
        assertNull(questionnaire.genayaSubjectNumber)
    }

    @Test
    fun canExtractFromMissingOrInvalidEntry() {
        assertNull(extraction().extract(null))
        assertNull(extraction().extract(entryWithText("Does not exist")))
    }

    private fun extraction(): QuestionnaireExtraction {
        return QuestionnaireExtraction(QuestionnaireRawEntryMapper(emptyMap()))
    }
}