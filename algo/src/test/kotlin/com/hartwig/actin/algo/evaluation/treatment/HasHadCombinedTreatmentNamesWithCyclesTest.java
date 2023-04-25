package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasHadCombinedTreatmentNamesWithCyclesTest {

    private static final PriorTumorTreatment MATCHING_PRIOR_TREATMENT = treatment("always MATCHing treatment", 11);
    private static final PriorTumorTreatment TEST_TREATMENT_WITH_WRONG_CYCLES = treatment("also test", 3);
    private static final PriorTumorTreatment TEST_TREATMENT_WITH_NULL_CYCLES = treatment("another TEST", null);
    public static final PriorTumorTreatment NON_MATCHING_TREATMENT = treatment("unknown", 10);

    private final HasHadCombinedTreatmentNamesWithCycles function =
            new HasHadCombinedTreatmentNamesWithCycles(List.of("Matching", "Test"), 8, 12);

    @Test
    public void shouldPassWhenAllQueryTreatmentNamesHaveAtLeastOneMatchWithRequiredCycles() {
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(patientRecordWithTreatmentHistory(List.of(MATCHING_PRIOR_TREATMENT,
                        treatment("TEST TREATMENT", 8),
                        TEST_TREATMENT_WITH_WRONG_CYCLES,
                        TEST_TREATMENT_WITH_NULL_CYCLES,
                        NON_MATCHING_TREATMENT))));
    }

    @Test
    public void shouldReturnUndeterminedWhenAnyQueryTreatmentNameHasAtLeastOneMatchWithNullCyclesAndNoneWithRequiredCycles() {
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(patientRecordWithTreatmentHistory(List.of(MATCHING_PRIOR_TREATMENT,
                        TEST_TREATMENT_WITH_WRONG_CYCLES,
                        TEST_TREATMENT_WITH_NULL_CYCLES,
                        NON_MATCHING_TREATMENT))));
    }

    @Test
    public void shouldFailWhenAnyQueryTreatmentNameHasAllMatchesWithKnownCycleCountOutsideRange() {
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(patientRecordWithTreatmentHistory(List.of(MATCHING_PRIOR_TREATMENT,
                        TEST_TREATMENT_WITH_WRONG_CYCLES,
                        NON_MATCHING_TREATMENT))));
    }

    @Test
    public void shouldFailWhenAnyQueryTreatmentNameHasNoMatchingTreatmentsInHistory() {
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(patientRecordWithTreatmentHistory(List.of(MATCHING_PRIOR_TREATMENT,
                        TEST_TREATMENT_WITH_WRONG_CYCLES,
                        NON_MATCHING_TREATMENT))));
    }

    private static PatientRecord patientRecordWithTreatmentHistory(List<PriorTumorTreatment> priorTumorTreatments) {
        PatientRecord minimal = TestDataFactory.createMinimalTestPatientRecord();
        ClinicalRecord clinicalRecord = ImmutableClinicalRecord.copyOf(minimal.clinical()).withPriorTumorTreatments(priorTumorTreatments);
        return ImmutablePatientRecord.copyOf(minimal).withClinical(clinicalRecord);
    }

    private static PriorTumorTreatment treatment(String name, @Nullable Integer numCycles) {
        return ImmutablePriorTumorTreatment.builder().name(name).isSystemic(true).cycles(numCycles).build();
    }
}