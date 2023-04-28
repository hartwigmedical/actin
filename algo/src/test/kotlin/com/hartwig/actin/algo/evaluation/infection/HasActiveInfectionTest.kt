package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus
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
            var infectionStatus: InfectionStatus? = null
            if (hasActiveInfection != null) {
                infectionStatus = ImmutableInfectionStatus.builder().hasActiveInfection(hasActiveInfection).build()
            }
            val base = TestClinicalFactory.createMinimalTestClinicalRecord()
            return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(
                    ImmutableClinicalRecord.builder()
                        .from(base)
                        .clinicalStatus(
                            ImmutableClinicalStatus.builder()
                                .from(base.clinicalStatus())
                                .infectionStatus(infectionStatus)
                                .build()
                        )
                        .build()
                )
                .build()
        }
    }
}