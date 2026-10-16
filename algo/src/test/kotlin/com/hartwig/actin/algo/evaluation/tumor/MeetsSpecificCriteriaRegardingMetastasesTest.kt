package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class MeetsSpecificCriteriaRegardingMetastasesTest {

    private val patientRecord = TumorTestFactory.withTumorStage(null)

    @Test
    fun `Should fail when no metastatic cancer`() {
        val alwaysFailsMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
            every { evaluate(any()) } returns EvaluationFactory.fail("no metastatic cancer")
        }
        val function = MeetsSpecificCriteriaRegardingMetastases(alwaysFailsMetastaticCancerEvaluation)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(patientRecord),
            "No metastatic cancer hence won't meet study specific criteria regarding metastases"
        )
    }

    @Test
    fun `Should evaluate to undetermined when unknown if patient has metastatic cancer`() {
        val alwaysUndeterminedMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
            every { evaluate(any()) } returns EvaluationFactory.undetermined("tumor stage unknown")
        }
        val function = MeetsSpecificCriteriaRegardingMetastases(alwaysUndeterminedMetastaticCancerEvaluation)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(patientRecord),
            "Undetermined if metastatic cancer and therefore undetermined if study specific criteria regarding metastases are met"
        )
    }

    @Test
    fun `Should evaluate to undetermined when patient has metastatic cancer`() {
        val alwaysPassMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
            every { evaluate(any()) } returns EvaluationFactory.pass("metastatic cancer")
        }
        val function = MeetsSpecificCriteriaRegardingMetastases(alwaysPassMetastaticCancerEvaluation)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(patientRecord),
            "Undetermined if study specific criteria regarding metastases are met"
        )
    }
}