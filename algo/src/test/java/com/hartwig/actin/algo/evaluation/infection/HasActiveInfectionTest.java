package com.hartwig.actin.algo.evaluation.infection;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasActiveInfectionTest {

    @Test
    public void canEvaluate() {
        HasActiveInfection function = new HasActiveInfection();

        assertEquals(EvaluationResult.UNDETERMINED, function.evaluate(withInfectionStatus(null)).result());
        assertEquals(EvaluationResult.PASS, function.evaluate(withInfectionStatus(true)).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(withInfectionStatus(false)).result());
    }

    @NotNull
    private static PatientRecord withInfectionStatus(@Nullable Boolean hasActiveInfection) {
        InfectionStatus infectionStatus = null;
        if (hasActiveInfection != null) {
            infectionStatus = ImmutableInfectionStatus.builder().hasActiveInfection(hasActiveInfection).description(Strings.EMPTY).build();
        }
        ClinicalRecord base = TestClinicalDataFactory.createMinimalTestClinicalRecord();
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