package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.tumor.HasLiverMetastases
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class IsEligibleForLocalLiverTreatmentTest {

    private val patientRecord = TestDataFactory.createMinimalTestPatientRecord()

    @Test
    fun `Should fail when no liver metastases`() {
        val alwaysFailsLiverMetastasesEvaluation = mockk<HasLiverMetastases> {
            every { evaluate(any()) } returns EvaluationFactory.fail("no liver metastases")
        }
        val function = IsEligibleForLocalLiverTreatment(alwaysFailsLiverMetastasesEvaluation)
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should be undetermined when data regarding liver metastases is missing`() {
        val alwaysUndeterminedLiverMetastasesEvaluation = mockk<HasLiverMetastases> {
            every { evaluate(any()) } returns EvaluationFactory.undetermined("data regarding liver metastases missing")
        }
        val function = IsEligibleForLocalLiverTreatment(alwaysUndeterminedLiverMetastasesEvaluation)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }

    @Test
    fun `Should be undetermined when patient has liver metastases`() {
        val alwaysPassLiverMetastasesEvaluation = mockk<HasLiverMetastases> {
            every { evaluate(any()) } returns EvaluationFactory.pass("liver metastases")
        }
        val function = IsEligibleForLocalLiverTreatment(alwaysPassLiverMetastasesEvaluation)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }
}