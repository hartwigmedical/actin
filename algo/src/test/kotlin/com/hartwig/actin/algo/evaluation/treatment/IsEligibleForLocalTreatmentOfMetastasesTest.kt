package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.treatment.IsEligibleForLocalTreatmentOfMetastases
import com.hartwig.actin.datamodel.algo.EvaluationResult
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class IsEligibleForLocalTreatmentOfMetastasesTest {

    private val patientRecord = TumorTestFactory.withTumorStage(null)

    @Test
    fun `Should fail when no metastatic cancer`() {
        val alwaysFailsMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
            every { evaluate(any()) } returns EvaluationFactory.fail("no metastatic cancer")
        }
        val function = IsEligibleForLocalTreatmentOfMetastases(alwaysFailsMetastaticCancerEvaluation)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should evaluate to undetermined when unknown if patient has metastatic cancer`() {
        val alwaysUndeterminedMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
            every { evaluate(any()) } returns EvaluationFactory.undetermined("tumor stage unknown")
        }
        val function = IsEligibleForLocalTreatmentOfMetastases(alwaysUndeterminedMetastaticCancerEvaluation)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }

    @Test
    fun `Should evaluate to undetermined when patient has metastatic cancer`() {
        val alwaysPassMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
            every { evaluate(any()) } returns EvaluationFactory.pass("metastatic cancer")
        }
        val function = IsEligibleForLocalTreatmentOfMetastases(alwaysPassMetastaticCancerEvaluation)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }
}