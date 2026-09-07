package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import org.junit.jupiter.api.Test

class HasSufficientLVEFTest {

    @Test
    fun canEvaluate() {
        val function = HasSufficientLVEF(0.71)

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withLVEF(null)), "LVEF unknown")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withLVEF(0.1)), "LVEF of 0.1 below 0.71")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withLVEF(0.71)), "LVEF of 0.71 exceeds 0.71")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withLVEF(0.9)), "LVEF of 0.9 exceeds 0.71")
    }

    companion object {
        private fun withLVEF(lvef: Double?): PatientRecord {
            return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(clinicalStatus = ClinicalStatus(lvef = lvef))
        }
    }
}