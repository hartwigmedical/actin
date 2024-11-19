package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
import org.junit.Test

class HasMinimumSitesWithLesionsTest {
    private val testPatient = patient(
        hasBoneLesions = true,
        hasSuspectedBoneLesions = false,
        hasBrainLesions = false,
        hasSuspectedBrainLesions = false,
        hasCnsLesions = false,
        hasSuspectedCnsLesions = false,
        hasLiverLesions = false,
        hasSuspectedLiverLesions = false,
        hasLungLesions = false,
        hasSuspectedLungLesions = false,
        hasLymphNodeLesions = true,
        hasSuspectedLymphNodeLesions = false,
        otherLesions = listOf("Prostate", "Subcutaneous"),
        otherSuspectedLesions = emptyList(),
        biopsyLocation = null
    )

    @Test
    fun `Should pass when number of categorized lesions equals threshold and no other lesions are present`() {
        assertEvaluation(
            EvaluationResult.PASS,
            HasMinimumSitesWithLesions(6).evaluate(
                patientWithConsistentLesionFlags(
                    lesionFlag = true,
                    suspectedLesionFlag = false,
                    otherLesions = emptyList(),
                    otherSuspectedLesions = emptyList(),
                    biopsyLocation = null
                )
            )
        )
    }

    @Test
    fun `Should pass when number of categorized lesions are one less than threshold and other lesions are present`() {
        assertEvaluation(EvaluationResult.PASS, HasMinimumSitesWithLesions(3).evaluate(testPatient))
    }

    @Test
    fun `Should warn when number of categorized lesions meets threshold when including suspected lesions`() {
        assertEvaluation(
            EvaluationResult.WARN,
            HasMinimumSitesWithLesions(4).evaluate(testPatient.copy(tumor = testPatient.tumor.copy(hasSuspectedLiverLesions = true)))
        )
    }

    @Test
    fun `Should be undetermined when threshold is between upper and lower lesion site limits`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, HasMinimumSitesWithLesions(5).evaluate(testPatient))
    }

    @Test
    fun `Should be undetermined when threshold is between upper and lower lesion site limits including suspected lesion`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasMinimumSitesWithLesions(6).evaluate(testPatient.copy(tumor = testPatient.tumor.copy(hasSuspectedLiverLesions = true)))
        )
    }

    @Test
    fun `Should fail when lesion site upper limit is less than threshold`() {
        assertEvaluation(EvaluationResult.FAIL, HasMinimumSitesWithLesions(6).evaluate(testPatient))
    }

    @Test
    fun `Should fail when lesion site upper limit including suspected lesions is less than threshold`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            HasMinimumSitesWithLesions(7).evaluate(testPatient.copy(tumor = testPatient.tumor.copy(hasSuspectedLiverLesions = true)))
        )
    }

    @Test
    fun `Should not count additional lesion details or biopsy location containing lymph when lymph node lesions present`() {
        val patient = TumorTestFactory.withTumorDetails(
            testPatient.tumor.copy(otherLesions = listOf("lymph node"), biopsyLocation = "lymph")
        )
        assertEvaluation(EvaluationResult.FAIL, HasMinimumSitesWithLesions(6).evaluate(patient))
    }

    @Test
    fun `Should not count null boolean fields or empty other lesions as sites`() {
        val patient = patientWithConsistentLesionFlags(null, null, emptyList(), emptyList(), null)
        assertEvaluation(EvaluationResult.UNDETERMINED, HasMinimumSitesWithLesions(1).evaluate(patient))
        assertEvaluation(EvaluationResult.FAIL, HasMinimumSitesWithLesions(2).evaluate(patient))
    }

    @Test
    fun `Should count biopsy location towards upper limit of lesion site count`() {
        val patient = patientWithConsistentLesionFlags(null, null, emptyList(), emptyList(), "Kidney")
        assertEvaluation(EvaluationResult.UNDETERMINED, HasMinimumSitesWithLesions(2).evaluate(patient))
        assertEvaluation(EvaluationResult.FAIL, HasMinimumSitesWithLesions(3).evaluate(patient))
    }

    companion object {
        private fun patientWithConsistentLesionFlags(
            lesionFlag: Boolean?,
            suspectedLesionFlag: Boolean?,
            otherLesions: List<String>?,
            otherSuspectedLesions: List<String>?,
            biopsyLocation: String?
        ): PatientRecord {
            return patient(
                lesionFlag,
                suspectedLesionFlag,
                lesionFlag,
                suspectedLesionFlag,
                lesionFlag,
                suspectedLesionFlag,
                lesionFlag,
                suspectedLesionFlag,
                lesionFlag,
                suspectedLesionFlag,
                lesionFlag,
                suspectedLesionFlag,
                otherLesions,
                otherSuspectedLesions,
                biopsyLocation
            )
        }

        private fun patient(
            hasBoneLesions: Boolean?,
            hasSuspectedBoneLesions: Boolean?,
            hasBrainLesions: Boolean?,
            hasSuspectedBrainLesions: Boolean?,
            hasCnsLesions: Boolean?,
            hasSuspectedCnsLesions: Boolean?,
            hasLiverLesions: Boolean?,
            hasSuspectedLiverLesions: Boolean?,
            hasLungLesions: Boolean?,
            hasSuspectedLungLesions: Boolean?,
            hasLymphNodeLesions: Boolean?,
            hasSuspectedLymphNodeLesions: Boolean?,
            otherLesions: List<String>?,
            otherSuspectedLesions: List<String>?,
            biopsyLocation: String?
        ): PatientRecord {
            return TumorTestFactory.withTumorDetails(
                TumorDetails(
                    hasBoneLesions = hasBoneLesions,
                    hasSuspectedBoneLesions = hasSuspectedBoneLesions,
                    hasBrainLesions = hasBrainLesions,
                    hasSuspectedBrainLesions = hasSuspectedBrainLesions,
                    hasCnsLesions = hasCnsLesions,
                    hasSuspectedCnsLesions = hasSuspectedCnsLesions,
                    hasLiverLesions = hasLiverLesions,
                    hasSuspectedLiverLesions = hasSuspectedLiverLesions,
                    hasLungLesions = hasLungLesions,
                    hasSuspectedLungLesions = hasSuspectedLungLesions,
                    hasLymphNodeLesions = hasLymphNodeLesions,
                    hasSuspectedLymphNodeLesions = hasSuspectedLymphNodeLesions,
                    otherLesions = otherLesions,
                    otherSuspectedLesions = otherSuspectedLesions,
                    biopsyLocation = biopsyLocation
                )
            )
        }
    }
}
