package com.hartwig.actin.algo.evaluation.infection;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasActiveInfectionTest {

    @Test
    public void canEvaluate() {
        HasActiveInfection function = new HasActiveInfection();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withInfectionStatus(null)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withInfectionStatus(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withInfectionStatus(false)));
    }

    @NotNull
    private static PatientRecord withInfectionStatus(@Nullable Boolean hasActiveInfection) {
        InfectionStatus infectionStatus = null;
        if (hasActiveInfection != null) {
            infectionStatus = ImmutableInfectionStatus.builder().hasActiveInfection(hasActiveInfection).build();
        }

        ClinicalRecord base = TestClinicalFactory.createMinimalTestClinicalRecord();
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(base)
                        .clinicalStatus(ImmutableClinicalStatus.builder()
                                .from(base.clinicalStatus())
                                .infectionStatus(infectionStatus)
                                .build())
                        .build())
                .build();
    }
}