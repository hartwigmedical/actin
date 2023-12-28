package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireExtraction.isActualQuestionnaire
import com.hartwig.actin.clinical.feed.questionnaire.TestQuestionnaireFactory.entryWithText
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class QuestionnaireExtractionTest {
    @Test
    fun `Should be able to determine that questionnaire entry is a questionnaire`() {
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
    fun `Should be able to handle missing GENAYA subject number from questionnaire`() {
        val questionnaire = questionnaire(
            TestQuestionnaireFactory.createTestQuestionnaireValueV1_6()
                .replace("GENAYA subjectno: GAYA-01-02-9999", "")
        )
        assertNull(questionnaire.genayaSubjectNumber)
    }

    @Test
    fun `Should be able to extract data from questionnaire v1_7`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV1_7())
        assertPatientHistory(questionnaire)
        assertClinical(questionnaire)
        assertMolecularTests(questionnaire)
        assertEquals("GAYA-01-02-9999", questionnaire.genayaSubjectNumber)
    }

    @Test
    fun `Should be able to extract data from questionnaire v1_6`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV1_6())
        assertPatientHistory(questionnaire)
        assertClinical(questionnaire)
        assertMolecularTests(questionnaire)
        assertEquals("GAYA-01-02-9999", questionnaire.genayaSubjectNumber)
    }

    @Test
    fun `Should be able to extract data from questionnaire v1_5`() {
        assertExtractionForQuestionnaireV1_5(TestQuestionnaireFactory.createTestQuestionnaireValueV1_5())
    }

    @Test
    fun `Should be able to extract data from alternate questionnaire v1_5`() {
        val rawQuestionnaire = TestQuestionnaireFactory.createTestQuestionnaireValueV1_5()
            .replace("- IHC test", "-IHC test")
            .replace("- PD L1 test", "-PD L1 test")
        assertExtractionForQuestionnaireV1_5(rawQuestionnaire)
    }

    @Test
    fun `Should be able to extract data from questionnaire v1_4`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV1_4())
        assertPatientHistory(questionnaire)
        assertClinicalBeforeV1_5(questionnaire)
        val ihcTestResults = questionnaire.ihcTestResults
        assertEquals(1, ihcTestResults!!.size.toLong())
        assertTrue(ihcTestResults.contains("IHC ERBB2 3+"))
        assertNull(questionnaire.pdl1TestResults)
    }

    @Test
    fun `Should be able to extract data from questionnaire v1_3`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV1_3())
        assertPatientHistory(questionnaire)
        assertClinicalBeforeV1_5(questionnaire)
        assertNull(questionnaire.ihcTestResults)
        assertNull(questionnaire.pdl1TestResults)
    }

    @Test
    fun `Should be able to extract data from questionnaire v1_2`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV1_2())
        assertPatientHistory(questionnaire)
        assertClinicalBeforeV1_5(questionnaire)
        assertNull(questionnaire.ihcTestResults)
        assertNull(questionnaire.pdl1TestResults)
    }

    @Test
    fun `Should be able to extract data from questionnaire v1_1`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV1_1())
        assertPatientHistory(questionnaire)
        assertClinicalBeforeV1_5(questionnaire)
        assertNull(questionnaire.ihcTestResults)
        assertNull(questionnaire.pdl1TestResults)
    }

    @Test
    fun `Should be able to extract data from questionnaire v1_0`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0())
        assertEquals(LocalDate.of(2020, 8, 28), questionnaire.date)
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
    fun `Should be able to extract data from questionnaire v0_2`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV0_2())
        assertEquals(LocalDate.of(2020, 8, 28), questionnaire.date)
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
    fun `Should be able to extract data from questionnaire v0_1`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1())
        assertEquals(LocalDate.of(2020, 8, 28), questionnaire.date)
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
    fun `Should extract from missing or invalid entry`() {
        val nullEntry = QuestionnaireExtraction.extract(null)
        assertThat(nullEntry.first).isNull()
        assertThat(nullEntry.second).isEmpty()
        val invalidEntry = QuestionnaireExtraction.extract(entryWithText("Does not exist"))
        assertThat(invalidEntry.first).isNull()
        assertThat(invalidEntry.second).isEmpty()
    }

    companion object {
        private fun assertPatientHistory(questionnaire: Questionnaire) {
            assertEquals(LocalDate.of(2020, 8, 28), questionnaire.date)
            val treatmentHistory = questionnaire.treatmentHistoryCurrentTumor
            assertEquals(2, treatmentHistory!!.size.toLong())
            assertTrue(treatmentHistory.contains("cisplatin"))
            assertTrue(treatmentHistory.contains("nivolumab"))

            val otherOncologicalHistory = questionnaire.otherOncologicalHistory
            assertEquals(1, otherOncologicalHistory!!.size.toLong())
            assertTrue(otherOncologicalHistory.contains("surgery"))

            val nonOncologicalHistory = questionnaire.nonOncologicalHistory
            assertEquals(1, nonOncologicalHistory!!.size.toLong())
            assertTrue(nonOncologicalHistory.contains("diabetes"))
        }

        private fun assertClinical(questionnaire: Questionnaire) {
            assertEquals("ovary", questionnaire.tumorLocation)
            assertEquals("serous", questionnaire.tumorType)
            assertEquals("lymph node", questionnaire.biopsyLocation)
            assertEquals(TumorStage.IV, questionnaire.stage)

            val secondaryPrimaries = questionnaire.secondaryPrimaries
            assertEquals(1, secondaryPrimaries!!.size.toLong())
            assertTrue(secondaryPrimaries.contains("sarcoma | Feb 2020"))

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
        }

        private fun assertClinicalBeforeV1_5(questionnaire: Questionnaire) {
            assertEquals("ovary", questionnaire.tumorLocation)
            assertEquals("serous", questionnaire.tumorType)
            assertEquals("Lymph node", questionnaire.biopsyLocation)
            assertEquals(TumorStage.III, questionnaire.stage)
            assertNull(questionnaire.secondaryPrimaries)

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

        private fun assertMolecularTests(questionnaire: Questionnaire) {
            val ihcTestResults = questionnaire.ihcTestResults
            assertEquals(1, ihcTestResults!!.size.toLong())
            assertTrue(ihcTestResults.contains("ERBB2 3+"))
            val pdl1TestResults = questionnaire.pdl1TestResults
            assertEquals(1, pdl1TestResults!!.size.toLong())
            assertTrue(pdl1TestResults.contains("Positive"))
        }

        private fun questionnaire(text: String): Questionnaire {
            return QuestionnaireExtraction.extract(entryWithText(text)).first!!
        }

        private fun assertExtractionForQuestionnaireV1_5(rawQuestionnaire: String) {
            val questionnaire = questionnaire(rawQuestionnaire)
            assertPatientHistory(questionnaire)
            assertClinical(questionnaire)
            assertMolecularTests(questionnaire)
            assertNull(questionnaire.genayaSubjectNumber)
        }
    }
}