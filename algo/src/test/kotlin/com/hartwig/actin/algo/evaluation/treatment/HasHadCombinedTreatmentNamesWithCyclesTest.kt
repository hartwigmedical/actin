package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableChemotherapy
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTherapyHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import org.junit.Test

class HasHadCombinedTreatmentNamesWithCyclesTest {

    private val function = HasHadCombinedTreatmentNamesWithCycles(
        listOf(chemotherapyWithName(TREATMENT_NAME_MATCHING), chemotherapyWithName(TREATMENT_NAME_TEST)), 8, 12
    )

    @Test
    fun shouldPassWhenAllQueryTreatmentNamesHaveAtLeastOneMatchWithRequiredCycles() {
        val treatmentHistory = listOf(
            MATCHING_PRIOR_TREATMENT,
            treatmentHistoryEntry(TREATMENT_NAME_TEST, 8),
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
        private const val TREATMENT_NAME_MATCHING = "Matching"
        private const val TREATMENT_NAME_TEST = "Test"

        private val MATCHING_PRIOR_TREATMENT = treatmentHistoryEntry(TREATMENT_NAME_MATCHING, 11)
        private val TEST_TREATMENT_WITH_WRONG_CYCLES = treatmentHistoryEntry(TREATMENT_NAME_TEST, 3)
        private val TEST_TREATMENT_WITH_NULL_CYCLES = treatmentHistoryEntry(TREATMENT_NAME_TEST, null)
        val NON_MATCHING_TREATMENT = treatmentHistoryEntry("unknown", 10)

        private fun patientRecordWithTreatmentHistory(treatmentHistory: List<TreatmentHistoryEntry>): PatientRecord {
            val minimal: PatientRecord = TestDataFactory.createMinimalTestPatientRecord()
            val clinicalRecord: ClinicalRecord =
                ImmutableClinicalRecord.copyOf(minimal.clinical()).withTreatmentHistory(treatmentHistory)
            return ImmutablePatientRecord.copyOf(minimal).withClinical(clinicalRecord)
        }

        private fun treatmentHistoryEntry(name: String, numCycles: Int?): TreatmentHistoryEntry {
            return ImmutableTreatmentHistoryEntry.builder()
                .addTreatments(chemotherapyWithName(name))
                .therapyHistoryDetails(ImmutableTherapyHistoryDetails.builder().cycles(numCycles).build())
                .build()
        }

        private fun chemotherapyWithName(name: String): ImmutableChemotherapy =
            ImmutableChemotherapy.builder().name(name).build()
    }
}