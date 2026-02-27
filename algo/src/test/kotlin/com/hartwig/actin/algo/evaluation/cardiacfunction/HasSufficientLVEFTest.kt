package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import org.junit.Test

class HasSufficientLVEFTest {

    @Test
    fun canEvaluate() {
        val function = HasSufficientLVEF(0.71)

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withLVEF(null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withLVEF(0.1)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withLVEF(0.71)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withLVEF(0.9)))
    }

    companion object {
        private fun withLVEF(lvef: Double?): PatientRecord {
            return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(clinicalStatus = ClinicalStatus(lvef = lvef))
        }
    }
}