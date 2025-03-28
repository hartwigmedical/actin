package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireExtraction.isActualQuestionnaire
import com.hartwig.actin.clinical.feed.emc.questionnaire.TestQuestionnaireFactory.entryWithText
import com.hartwig.actin.datamodel.clinical.TumorStage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class QuestionnaireExtractionTest {
    @Test
    fun `Should be able to determine that questionnaire entry is a questionnaire`() {
        assertThat(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_7()))).isTrue
        assertThat(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_6()))).isTrue
        assertThat(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_5()))).isTrue
        assertThat(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_4()))).isTrue
        assertThat(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_3()))).isTrue
        assertThat(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_2()))).isTrue
        assertThat(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_1()))).isTrue
        assertThat(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0()))).isTrue
        assertThat(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV0_2()))).isTrue
        assertThat(isActualQuestionnaire(entryWithText(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1()))).isTrue
        assertThat(isActualQuestionnaire(entryWithText("Does not exist"))).isFalse
    }

    @Test
    fun `Should be able to extract data from questionnaire v1_7`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV1_7())
        assertPatientHistory(questionnaire)
        assertClinical(questionnaire)
        assertMolecularTests(questionnaire)
    }

    @Test
    fun `Should be able to extract data from questionnaire v1_6`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV1_6())
        assertPatientHistory(questionnaire)
        assertClinical(questionnaire)
        assertMolecularTests(questionnaire)
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
        assertThat(ihcTestResults!!).hasSize(1)
        assertThat(ihcTestResults).contains("IHC ERBB2 3+")
        assertThat(questionnaire.pdl1TestResults).isNull()
    }

    @Test
    fun `Should be able to extract data from questionnaire v1_3`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV1_3())
        assertPatientHistory(questionnaire)
        assertClinicalBeforeV1_5(questionnaire)
        assertThat(questionnaire.ihcTestResults).isNull()
        assertThat(questionnaire.pdl1TestResults).isNull()
    }

    @Test
    fun `Should be able to extract data from questionnaire v1_2`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV1_2())
        assertPatientHistory(questionnaire)
        assertClinicalBeforeV1_5(questionnaire)
        assertThat(questionnaire.ihcTestResults).isNull()
        assertThat(questionnaire.pdl1TestResults).isNull()
    }

    @Test
    fun `Should be able to extract data from questionnaire v1_1`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV1_1())
        assertPatientHistory(questionnaire)
        assertClinicalBeforeV1_5(questionnaire)
        assertThat(questionnaire.ihcTestResults).isNull()
        assertThat(questionnaire.pdl1TestResults).isNull()
    }

    @Test
    fun `Should be able to extract data from questionnaire v1_0`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV1_0())
        assertThat(questionnaire.date).isEqualTo(LocalDate.of(2020, 8, 28))
        assertThat(questionnaire.tumorLocation).isEqualTo("lung")
        assertThat(questionnaire.tumorType).isEqualTo("small-cell carcinoma")
        assertThat(questionnaire.biopsyLocation).isEqualTo("Liver")
        val treatmentHistory = questionnaire.treatmentHistoryCurrentTumor
        assertThat(treatmentHistory!!).hasSize(1)
        assertThat(treatmentHistory).contains("capecitabine JAN 2020- JUL 2021")
        val otherOncologicalHistory = questionnaire.otherOncologicalHistory
        assertThat(otherOncologicalHistory!!).hasSize(1)
        assertThat(otherOncologicalHistory).contains("surgery JUN 2021")
        assertThat(questionnaire.secondaryPrimaries).isNull()
        val nonOncologicalHistory = questionnaire.nonOncologicalHistory
        assertThat(nonOncologicalHistory!!).hasSize(1)
        assertThat(nonOncologicalHistory).contains("NO")
        assertThat(questionnaire.stage).isEqualTo(TumorStage.IV)
        assertThat(questionnaire.hasMeasurableDisease!!).isTrue
        assertThat(questionnaire.hasBrainLesions).isNull()
        assertThat(questionnaire.hasActiveBrainLesions).isNull()
        assertThat(questionnaire.hasCnsLesions!!).isFalse
        assertThat(questionnaire.hasActiveCnsLesions).isNull()
        assertThat(questionnaire.hasBoneLesions!!).isFalse
        assertThat(questionnaire.hasLiverLesions!!).isFalse
        val otherLesions = questionnaire.otherLesions
        assertThat(otherLesions!!).hasSize(3)
        assertThat(otherLesions).contains("peritoneal")
        assertThat(otherLesions).contains("lymph nodes")
        assertThat(otherLesions).contains("lung")
        assertThat(questionnaire.ihcTestResults).isNull()
        assertThat(questionnaire.pdl1TestResults).isNull()
        assertThat((questionnaire.whoStatus as Int).toLong()).isEqualTo(1)
        val unresolvedToxicities = questionnaire.unresolvedToxicities
        assertThat(unresolvedToxicities!!).hasSize(1)
        assertThat(unresolvedToxicities).contains("NA")
        val infectionStatus = questionnaire.infectionStatus
        assertThat(infectionStatus).isNotNull()
        assertThat(infectionStatus!!.hasActiveInfection).isFalse
        assertThat(questionnaire.ecg).isNull()
        val complications = questionnaire.complications
        assertThat(complications!!).hasSize(1)
        assertThat(complications).contains("ascites")
    }

    @Test
    fun `Should be able to extract data from questionnaire v0_2`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV0_2())
        assertThat(questionnaire.date).isEqualTo(LocalDate.of(2020, 8, 28))
        assertThat(questionnaire.tumorLocation).isEqualTo("cholangio")
        assertThat(questionnaire.tumorType).isEqualTo("carcinoma")
        assertThat(questionnaire.biopsyLocation).isEqualTo("liver")
        val treatmentHistory = questionnaire.treatmentHistoryCurrentTumor
        assertThat(treatmentHistory!!).hasSize(1)
        assertThat(treatmentHistory).contains("capecitabine")
        val otherOncologicalHistory = questionnaire.otherOncologicalHistory
        assertThat(otherOncologicalHistory!!).hasSize(1)
        assertThat(otherOncologicalHistory).contains("radiotherapy")
        assertThat(questionnaire.secondaryPrimaries).isNull()
        val nonOncologicalHistory = questionnaire.nonOncologicalHistory
        assertThat(nonOncologicalHistory!!).hasSize(1)
        assertThat(nonOncologicalHistory).contains("NA")
        assertThat(questionnaire.stage).isEqualTo(TumorStage.IV)
        assertThat(questionnaire.hasMeasurableDisease!!).isTrue
        assertThat(questionnaire.hasBrainLesions).isNull()
        assertThat(questionnaire.hasActiveBrainLesions).isNull()
        assertThat(questionnaire.hasCnsLesions).isNull()
        assertThat(questionnaire.hasActiveCnsLesions).isNull()
        assertThat(questionnaire.hasBoneLesions!!).isFalse
        assertThat(questionnaire.hasLiverLesions!!).isFalse
        assertThat(questionnaire.otherLesions).isNull()
        assertThat(questionnaire.ihcTestResults).isNull()
        assertThat(questionnaire.pdl1TestResults).isNull()
        assertThat((questionnaire.whoStatus as Int).toLong()).isEqualTo(2)
        assertThat(questionnaire.unresolvedToxicities).isNull()
        val infectionStatus = questionnaire.infectionStatus
        assertThat(infectionStatus).isNotNull()
        assertThat(infectionStatus!!.hasActiveInfection).isFalse
        assertThat(questionnaire.ecg).isNull()
        val complications = questionnaire.complications
        assertThat(complications!!).hasSize(1)
        assertThat(complications).contains("pleural effusion")
    }

    @Test
    fun `Should be able to extract data from questionnaire v0_1`() {
        val questionnaire = questionnaire(TestQuestionnaireFactory.createTestQuestionnaireValueV0_1())
        assertThat(questionnaire.date).isEqualTo(LocalDate.of(2020, 8, 28))
        assertThat(questionnaire.tumorLocation).isEqualTo("Cholangiocarcinoom (lever, lymph retroperitoneaal)")
        assertThat(questionnaire.tumorType).isEqualTo("Unknown")
        assertThat(questionnaire.biopsyLocation).isNull()
        assertThat(questionnaire.treatmentHistoryCurrentTumor).isNull()
        assertThat(questionnaire.otherOncologicalHistory).isNull()
        assertThat(questionnaire.secondaryPrimaries).isNull()
        val nonOncologicalHistory = questionnaire.nonOncologicalHistory
        assertThat(nonOncologicalHistory!!).hasSize(1)
        assertThat(nonOncologicalHistory).contains("Diabetes Mellitus type 2")
        assertThat(questionnaire.stage).isNull()
        assertThat(questionnaire.hasMeasurableDisease!!).isTrue
        assertThat(questionnaire.hasBrainLesions).isNull()
        assertThat(questionnaire.hasActiveBrainLesions).isNull()
        assertThat(questionnaire.hasCnsLesions).isNull()
        assertThat(questionnaire.hasActiveCnsLesions).isNull()
        assertThat(questionnaire.hasBoneLesions!!).isTrue
        assertThat(questionnaire.hasLiverLesions!!).isTrue
        val otherLesions = questionnaire.otherLesions
        assertThat(otherLesions!!).hasSize(2)
        assertThat(otherLesions).contains("lever")
        assertThat(otherLesions).contains("lymph retroperitoneaal")
        assertThat(questionnaire.ihcTestResults).isNull()
        assertThat(questionnaire.pdl1TestResults).isNull()
        assertThat((questionnaire.whoStatus as Int).toLong()).isEqualTo(1)
        val unresolvedToxicities = questionnaire.unresolvedToxicities
        assertThat(unresolvedToxicities!!).hasSize(1)
        assertThat(unresolvedToxicities).contains("Neuropathy GR3")
        val infectionStatus = questionnaire.infectionStatus
        assertThat(infectionStatus).isNotNull()
        assertThat(infectionStatus!!.hasActiveInfection).isFalse
        val ecg = questionnaire.ecg
        assertThat(ecg).isNull()
        assertThat(questionnaire.complications).isNull()
    }

    @Test
    fun `Should extract from missing or invalid entry`() {
        val nullEntry = QuestionnaireExtraction.extract(emptyList())
        assertThat(nullEntry.first).isNull()
        assertThat(nullEntry.second).isEmpty()
        val invalidEntry = QuestionnaireExtraction.extract(listOf(entryWithText("Does not exist")))
        assertThat(invalidEntry.first).isNull()
        assertThat(invalidEntry.second).isEmpty()
    }

    @Test
    fun `Should reject new empty questionnaire in favor of an old one`() {
        val newInvalidEntry = entryWithText("Does not exist").copy(authored = LocalDate.now())
        val oldValidEntry = TestQuestionnaireFactory.createTestQuestionnaireEntry().copy(
            authored = LocalDate.now().minusDays(1),
            text = TestQuestionnaireFactory.createTestQuestionnaireValueV1_7().replace("\n", "\\n")
        )
        val extractedEntry = QuestionnaireExtraction.extract(listOf(newInvalidEntry, oldValidEntry))
        assertThat(extractedEntry.first).isNotNull
    }

    companion object {
        private fun assertPatientHistory(questionnaire: Questionnaire) {
            assertThat(questionnaire.date).isEqualTo(LocalDate.of(2020, 8, 28))
            val treatmentHistory = questionnaire.treatmentHistoryCurrentTumor
            assertThat(treatmentHistory!!).hasSize(2)
            assertThat(treatmentHistory).contains("cisplatin")
            assertThat(treatmentHistory).contains("nivolumab")

            val otherOncologicalHistory = questionnaire.otherOncologicalHistory
            assertThat(otherOncologicalHistory!!).hasSize(1)
            assertThat(otherOncologicalHistory).contains("surgery")

            val nonOncologicalHistory = questionnaire.nonOncologicalHistory
            assertThat(nonOncologicalHistory!!).hasSize(1)
            assertThat(nonOncologicalHistory).contains("diabetes")
        }

        private fun assertClinical(questionnaire: Questionnaire) {
            assertThat(questionnaire.tumorLocation).isEqualTo("ovary")
            assertThat(questionnaire.tumorType).isEqualTo("serous")
            assertThat(questionnaire.biopsyLocation).isEqualTo("lymph node")
            assertThat(questionnaire.stage).isEqualTo(TumorStage.IV)

            val secondaryPrimaries = questionnaire.secondaryPrimaries
            assertThat(secondaryPrimaries!!).hasSize(1)
            assertThat(secondaryPrimaries).contains("sarcoma | last treatment date: Feb 2020")

            assertThat(questionnaire.hasMeasurableDisease!!).isTrue
            assertThat(questionnaire.hasBrainLesions!!).isTrue
            assertThat(questionnaire.hasActiveBrainLesions!!).isTrue
            assertThat(questionnaire.hasCnsLesions).isNull()
            assertThat(questionnaire.hasActiveCnsLesions).isNull()
            assertThat(questionnaire.hasBoneLesions!!).isFalse
            assertThat(questionnaire.hasLiverLesions!!).isFalse

            val otherLesions = questionnaire.otherLesions
            assertThat(otherLesions!!).hasSize(2)
            assertThat(otherLesions).contains("pulmonal")
            assertThat(otherLesions).contains("abdominal")

            assertThat((questionnaire.whoStatus as Int).toLong()).isEqualTo(0)

            val unresolvedToxicities = questionnaire.unresolvedToxicities
            assertThat(unresolvedToxicities!!).hasSize(1)
            assertThat(unresolvedToxicities).contains("toxic")

            val infectionStatus = questionnaire.infectionStatus
            assertThat(infectionStatus).isNotNull()
            assertThat(infectionStatus!!.hasActiveInfection).isFalse

            assertThat(questionnaire.ecg?.name).isEqualTo("Sinus")

            val complications = questionnaire.complications
            assertThat(complications!!).hasSize(1)
            assertThat(complications).contains("vomit")
        }

        private fun assertClinicalBeforeV1_5(questionnaire: Questionnaire) {
            assertThat(questionnaire.tumorLocation).isEqualTo("ovary")
            assertThat(questionnaire.tumorType).isEqualTo("serous")
            assertThat(questionnaire.biopsyLocation).isEqualTo("Lymph node")
            assertThat(questionnaire.stage).isEqualTo(TumorStage.III)
            assertThat(questionnaire.secondaryPrimaries).isNull()

            assertThat(questionnaire.hasMeasurableDisease!!).isTrue
            assertThat(questionnaire.hasBrainLesions).isNull()
            assertThat(questionnaire.hasActiveBrainLesions).isNull()
            assertThat(questionnaire.hasCnsLesions).isNull()
            assertThat(questionnaire.hasActiveCnsLesions).isNull()
            assertThat(questionnaire.hasBoneLesions!!).isFalse
            assertThat(questionnaire.hasLiverLesions!!).isFalse

            val otherLesions = questionnaire.otherLesions
            assertThat(otherLesions!!).hasSize(1)
            assertThat(otherLesions).contains("pulmonal")

            assertThat(questionnaire.whoStatus).isEqualTo(0)
            assertThat(questionnaire.unresolvedToxicities).isNull()

            val infectionStatus = questionnaire.infectionStatus
            assertThat(infectionStatus).isNotNull()
            assertThat(infectionStatus!!.hasActiveInfection).isFalse

            assertThat(questionnaire.ecg?.name).isEqualTo("Sinus")

            val complications = questionnaire.complications
            assertThat(complications!!).hasSize(1)
            assertThat(complications).contains("nausea")
        }

        private fun assertMolecularTests(questionnaire: Questionnaire) {
            val ihcTestResults = questionnaire.ihcTestResults
            assertThat(ihcTestResults!!).hasSize(1)
            assertThat(ihcTestResults).contains("ERBB2 3+")
            val pdl1TestResults = questionnaire.pdl1TestResults
            assertThat(pdl1TestResults!!).hasSize(1)
            assertThat(pdl1TestResults).contains("Positive")
        }

        private fun questionnaire(text: String): Questionnaire {
            return QuestionnaireExtraction.extract(listOf(entryWithText(text.replace("\n", "\\n")))).first!!
        }

        private fun assertExtractionForQuestionnaireV1_5(rawQuestionnaire: String) {
            val questionnaire = questionnaire(rawQuestionnaire)
            assertPatientHistory(questionnaire)
            assertClinical(questionnaire)
            assertMolecularTests(questionnaire)
        }
    }
}