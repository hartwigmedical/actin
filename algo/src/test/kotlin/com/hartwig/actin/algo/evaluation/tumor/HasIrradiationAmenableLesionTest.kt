package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class HasIrradiationAmenableLesionTest {

    private val patientRecord = TumorTestFactory.withTumorStage(null)

    @Test
    fun `Should fail when no metastatic cancer`() {
        val alwaysFailsMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
            every { evaluate(any()) } returns EvaluationFactory.fail("no metastatic cancer")
        }
        val function = HasIrradiationAmenableLesion(alwaysFailsMetastaticCancerEvaluation)
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should evaluate to undetermined when unknown if patient has metastatic cancer`() {
        val alwaysUndeterminedMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
            every { evaluate(any()) } returns EvaluationFactory.undetermined("tumor stage unknown")
        }
        val function = HasIrradiationAmenableLesion(alwaysUndeterminedMetastaticCancerEvaluation)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }

    @Test
    fun `Should evaluate to undetermined when patient has metastatic cancer`() {
        val alwaysPassMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
            every { evaluate(any()) } returns EvaluationFactory.pass("metastatic cancer")
        }
        val function = HasIrradiationAmenableLesion(alwaysPassMetastaticCancerEvaluation)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }
}