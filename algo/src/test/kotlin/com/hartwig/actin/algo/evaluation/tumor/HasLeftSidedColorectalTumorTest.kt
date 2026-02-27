package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory.withDoids
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasLeftSidedColorectalTumorTest {
    
    @Test
    fun `Should return undetermined when no tumor DOIDs configured`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should fail when tumor is not colorectal`() {
        assertEvaluation(EvaluationResult.FAIL, function().evaluate(withDoids(DoidConstants.PROSTATE_CANCER_DOID)))
    }

    @Test
    fun `Should pass when name does contain a left string`() {
        HasLeftSidedColorectalTumor.LEFT_SUB_LOCATIONS.forEach { name: String ->
            assertEvaluation(EvaluationResult.PASS, function().evaluate(patientWithTumorName("text $name other text")))
        }
    }

    @Test
    fun `Should pass when name contains rectum`() {
        assertEvaluation(EvaluationResult.PASS, function().evaluate(patientWithTumorName(("rectum"))))
    }

    @Test
    fun `Should fail when name contains a right string`() {
        HasLeftSidedColorectalTumor.RIGHT_SUB_LOCATIONS.forEach { name: String? ->
            assertEvaluation(EvaluationResult.FAIL, function().evaluate(patientWithTumorName(("text $name other text"))))
        }
    }

    @Test
    fun `Should fail when name contains colorectum cecum`() {
        assertEvaluation(EvaluationResult.FAIL, function().evaluate(patientWithTumorName(("colorectum cecum"))))
    }

    @Test
    fun `Should be undetermined when name does not contain a known left or right string`() {
        listOf("", "unknown", "some string").forEach { name: String ->
            assertEvaluation(
                EvaluationResult.UNDETERMINED,
                function().evaluate(patientWithTumorName(name))
            )
        }
    }

    companion object {
        private fun patientWithTumorName(name: String): PatientRecord {
            return TumorTestFactory.withDoidAndName(DoidConstants.COLORECTAL_CANCER_DOID, name)
        }

        private fun function(): HasLeftSidedColorectalTumor {
            val doidModel: DoidModel =
                TestDoidModelFactory.createWithOneDoidAndTerm(DoidConstants.COLORECTAL_CANCER_DOID, "colorectal cancer")
            return HasLeftSidedColorectalTumor(doidModel)
        }
    }
}