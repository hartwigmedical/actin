package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.treatment.ImmutablePriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasHadCombinedTreatmentNamesWithCyclesTest {

    private val function = HasHadCombinedTreatmentNamesWithCycles(listOf("Matching", "Test"), 8, 12)

    @Test
    fun shouldPassWhenAllQueryTreatmentNamesHaveAtLeastOneMatchWithRequiredCycles() {
        val treatmentHistory = listOf(
            MATCHING_PRIOR_TREATMENT,
            treatment("TEST TREATMENT", 8),
            TEST_TREATMENT_WITH_WRONG_CYCLES,
            TEST_TREATMENT_WITH_NULL_CYCLES,
            NON_MATCHING_TREATMENT
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(patientRecordWithTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun shouldReturnUndeterminedWhenAnyQueryTreatmentNameHasAtLeastOneMatchWithNullCyclesAndNoneWithRequiredCycles() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                patientRecordWithTreatmentHistory(
                    listOf(
                        MATCHING_PRIOR_TREATMENT,
                        TEST_TREATMENT_WITH_WRONG_CYCLES, TEST_TREATMENT_WITH_NULL_CYCLES, NON_MATCHING_TREATMENT
                    )
                )
            )
        )
    }

    @Test
    fun shouldFailWhenAnyQueryTreatmentNameHasAllMatchesWithKnownCycleCountOutsideRange() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                patientRecordWithTreatmentHistory(
                    listOf(
                        MATCHING_PRIOR_TREATMENT,
                        TEST_TREATMENT_WITH_WRONG_CYCLES, NON_MATCHING_TREATMENT
                    )
                )
            )
        )
    }

    @Test
    fun shouldFailWhenAnyQueryTreatmentNameHasNoMatchingTreatmentsInHistory() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                patientRecordWithTreatmentHistory(
                    listOf(
                        MATCHING_PRIOR_TREATMENT,
                        TEST_TREATMENT_WITH_WRONG_CYCLES, NON_MATCHING_TREATMENT
                    )
                )
            )
        )
    }

    companion object {
        private val MATCHING_PRIOR_TREATMENT: PriorTumorTreatment = treatment("always MATCHing treatment", 11)
        private val TEST_TREATMENT_WITH_WRONG_CYCLES: PriorTumorTreatment = treatment("also test", 3)
        private val TEST_TREATMENT_WITH_NULL_CYCLES: PriorTumorTreatment = treatment("another TEST", null)
        val NON_MATCHING_TREATMENT: PriorTumorTreatment = treatment("unknown", 10)

        private fun patientRecordWithTreatmentHistory(priorTumorTreatments: List<PriorTumorTreatment>): PatientRecord {
            val minimal: PatientRecord = TestDataFactory.createMinimalTestPatientRecord()
            val clinicalRecord: ClinicalRecord =
                ImmutableClinicalRecord.copyOf(minimal.clinical()).withPriorTumorTreatments(priorTumorTreatments)
            return ImmutablePatientRecord.copyOf(minimal).withClinical(clinicalRecord)
        }

        private fun treatment(name: String, numCycles: Int?): PriorTumorTreatment {
            return ImmutablePriorTumorTreatment.builder().name(name).isSystemic(true).cycles(numCycles).build()
        }
    }
}