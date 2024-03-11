package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.InfectionStatus
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import org.junit.Test

class HasActiveInfectionTest {

    @Test
    fun canEvaluate() {
        val function = HasActiveInfection()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withInfectionStatus(null)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withInfectionStatus(true)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withInfectionStatus(false)))
    }

    companion object {
        private fun withInfectionStatus(hasActiveInfection: Boolean?): PatientRecord {
            val infectionStatus = hasActiveInfection?.let { InfectionStatus(hasActiveInfection = it, description = null) }
            val base = TestClinicalFactory.createMinimalTestClinicalRecord()
            return TestDataFactory.createMinimalTestPatientRecord().copy(
                clinicalStatus = base.clinicalStatus.copy(infectionStatus = infectionStatus)
            )
        }
    }
}