package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TumorDetails
import org.junit.Test

class HasMinimumSitesWithLesionsTest {
    private val testPatient = patient(true, false, false, false, false, true, listOf("Prostate", "Subcutaneous"), null)

    @Test
    fun shouldPassWhenNumberOfCategorizedLesionsEqualThresholdAndNoOtherLesionsArePresent() {
        assertEvaluation(
            EvaluationResult.PASS,
            HasMinimumSitesWithLesions(6).evaluate(patientWithConsistentLesionFlags(true, emptyList(), null))
        )
    }

    @Test
    fun shouldPassWhenNumberOfCategorizedLesionsAreOneLessThanThresholdAndOtherLesionsArePresent() {
        assertEvaluation(EvaluationResult.PASS, HasMinimumSitesWithLesions(3).evaluate(testPatient))
    }

    @Test
    fun shouldBeUndeterminedWhenThresholdIsBetweenUpperAndLowerLesionSiteLimits() {
        assertEvaluation(EvaluationResult.UNDETERMINED, HasMinimumSitesWithLesions(4).evaluate(testPatient))
    }

    @Test
    fun shouldBeUndeterminedWhenThresholdIsOneMoreThanCountOfAllCategorizedAndUncategorizedLesionLocations() {
        assertEvaluation(EvaluationResult.UNDETERMINED, HasMinimumSitesWithLesions(5).evaluate(testPatient))
    }

    @Test
    fun shouldFailWhenLesionSiteUpperLimitIsLessThanThreshold() {
        assertEvaluation(EvaluationResult.FAIL, HasMinimumSitesWithLesions(6).evaluate(testPatient))
    }

    @Test
    fun shouldNotCountAdditionalLesionDetailsOrBiopsyLocationContainingLymphWhenLymphNodeLesionsPresent() {
        val patient = TestTumorFactory.withTumorDetails(
            testPatient.tumor.copy(otherLesions = listOf("lymph node"), biopsyLocation = "lymph")
        )
        assertEvaluation(EvaluationResult.FAIL, HasMinimumSitesWithLesions(6).evaluate(patient))
    }

    @Test
    fun shouldNotCountNullBooleanFieldsOrEmptyOtherLesionsAsSites() {
        val patient = patientWithConsistentLesionFlags(null, emptyList(), null)
        assertEvaluation(EvaluationResult.UNDETERMINED, HasMinimumSitesWithLesions(1).evaluate(patient))
        assertEvaluation(EvaluationResult.FAIL, HasMinimumSitesWithLesions(2).evaluate(patient))
    }

    @Test
    fun shouldCountBiopsyLocationTowardsUpperLimitOfLesionSiteCount() {
        val patient = patientWithConsistentLesionFlags(null, emptyList(), "Kidney")
        assertEvaluation(EvaluationResult.UNDETERMINED, HasMinimumSitesWithLesions(2).evaluate(patient))
        assertEvaluation(EvaluationResult.FAIL, HasMinimumSitesWithLesions(3).evaluate(patient))
    }

    companion object {
        private fun patientWithConsistentLesionFlags(
            lesionFlag: Boolean?,
            otherLesions: List<String>?,
            biopsyLocation: String?
        ): PatientRecord {
            return patient(lesionFlag, lesionFlag, lesionFlag, lesionFlag, lesionFlag, lesionFlag, otherLesions, biopsyLocation)
        }

        private fun patient(
            hasBoneLesions: Boolean?, hasBrainLesions: Boolean?, hasCnsLesions: Boolean?, hasLiverLesions: Boolean?,
            hasLungLesions: Boolean?, hasLymphNodeLesions: Boolean?, otherLesions: List<String>?, biopsyLocation: String?
        ): PatientRecord {
            return TestTumorFactory.withTumorDetails(
                TumorDetails(
                    hasBoneLesions = hasBoneLesions,
                    hasBrainLesions = hasBrainLesions,
                    hasCnsLesions = hasCnsLesions,
                    hasLiverLesions = hasLiverLesions,
                    hasLungLesions = hasLungLesions,
                    hasLymphNodeLesions = hasLymphNodeLesions,
                    otherLesions = otherLesions,
                    biopsyLocation = biopsyLocation
                )
            )
        }
    }
}