package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
import org.junit.jupiter.api.Test

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
                )
            ),
            "Has at least 6 lesion sites"
        )
    }

    @Test
    fun `Should pass when number of categorized lesions are one less than threshold and other lesions are present`() {
        assertEvaluation(EvaluationResult.PASS, HasMinimumSitesWithLesions(3).evaluate(testPatient), "Has at least 3 lesion sites")
    }

    @Test
    fun `Should warn when number of categorized lesions meets threshold when including suspected lesions`() {
        assertEvaluation(
            EvaluationResult.WARN,
            HasMinimumSitesWithLesions(4).evaluate(testPatient.copy(tumor = testPatient.tumor.copy(hasSuspectedLiverLesions = true))),
            "Has at least 4 lesion sites (when including suspected lesions)"
        )
    }

    @Test
    fun `Should be undetermined when threshold is between upper and lower lesion site limits`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasMinimumSitesWithLesions(5).evaluate(testPatient),
            "Undetermined if sufficient lesion sites (near threshold of 5)"
        )
    }

    @Test
    fun `Should be undetermined when threshold is between upper and lower lesion site limits including suspected lesion`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasMinimumSitesWithLesions(6).evaluate(testPatient.copy(tumor = testPatient.tumor.copy(hasSuspectedLiverLesions = true))),
            "Undetermined if sufficient lesion sites (near threshold of 6 when including suspected lesions)"
        )
    }

    @Test
    fun `Should fail when lesion site upper limit is less than threshold`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            HasMinimumSitesWithLesions(6).evaluate(testPatient),
            "Insufficient number of lesion sites (less than 6)"
        )
    }

    @Test
    fun `Should fail when lesion site upper limit including suspected lesions is less than threshold`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            HasMinimumSitesWithLesions(7).evaluate(testPatient.copy(tumor = testPatient.tumor.copy(hasSuspectedLiverLesions = true))),
            "Insufficient number of lesion sites (less than 7)"
        )
    }

    @Test
    fun `Should not count null boolean fields or empty other lesions as sites`() {
        val patient = patientWithConsistentLesionFlags(null, null, emptyList(), emptyList())
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasMinimumSitesWithLesions(1).evaluate(patient),
            "Undetermined if sufficient lesion sites (near threshold of 1)"
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            HasMinimumSitesWithLesions(2).evaluate(patient),
            "Insufficient number of lesion sites (less than 2)"
        )
    }

    companion object {
        private fun patientWithConsistentLesionFlags(
            lesionFlag: Boolean?,
            suspectedLesionFlag: Boolean?,
            otherLesions: List<String>?,
            otherSuspectedLesions: List<String>?,
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
                )
            )
        }
    }
}
