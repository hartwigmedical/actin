package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory.withDoids
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasLeftSidedColorectalTumorTest {
    @Test
    fun shouldReturnUndeterminedWhenNoTumorDoidsConfigured() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(TestPatientFactory.createMinimalTestPatientRecord()))
    }

    @Test
    fun shouldFailWhenTumorIsNotColorectal() {
        assertEvaluation(EvaluationResult.FAIL, function().evaluate(withDoids(DoidConstants.PROSTATE_CANCER_DOID)))
    }

    @Test
    fun shouldReturnUndeterminedWhenTumorSubLocationIsUnknownOrMissing() {
        listOf(null, "", "unknown")
            .forEach { subLocation: String? ->
                assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(patientWithTumorSubLocation(subLocation)))
            }
    }

    @Test
    fun shouldPassWhenLeftTumorSubLocationProvided() {
        listOf("Rectum", "Descending Colon", "COLON sigmoid", "colon descendens", "rectosigmoid", "Colon sigmoideum")
            .forEach { subLocation: String? ->
                assertEvaluation(EvaluationResult.PASS, function().evaluate(patientWithTumorSubLocation(subLocation)))
            }
    }

    @Test
    fun shouldFailWhenRightTumorSubLocationProvided() {
        listOf(
            "Ascending colon",
            "Colon ascendens",
            "caecum",
            "cecum",
            "transverse COLON",
            "colon transversum",
            "flexura hepatica",
            "hepatic flexure"
        )
            .forEach { subLocation: String? ->
                assertEvaluation(EvaluationResult.FAIL, function().evaluate(patientWithTumorSubLocation(subLocation)))
            }
    }

    companion object {
        private fun patientWithTumorSubLocation(subLocation: String?): PatientRecord {
            return TumorTestFactory.withDoidAndSubLocation(DoidConstants.COLORECTAL_CANCER_DOID, subLocation)
        }

        private fun function(): HasLeftSidedColorectalTumor {
            val doidModel: DoidModel =
                TestDoidModelFactory.createWithOneDoidAndTerm(DoidConstants.COLORECTAL_CANCER_DOID, "colorectal cancer")
            return HasLeftSidedColorectalTumor(doidModel)
        }
    }
}