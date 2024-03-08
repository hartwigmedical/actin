package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import org.junit.Test

class HasSufficientLVEFTest {

    @Test
    fun canEvaluate() {
        val function = HasSufficientLVEF(0.71)

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(withLVEF(null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withLVEF(0.1)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withLVEF(0.71)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withLVEF(0.9)))
    }

    companion object {
        private fun withLVEF(lvef: Double?): PatientRecord {
            return TestDataFactory.createMinimalTestPatientRecord().copy(clinicalStatus = ClinicalStatus(lvef = lvef))
        }
    }
}