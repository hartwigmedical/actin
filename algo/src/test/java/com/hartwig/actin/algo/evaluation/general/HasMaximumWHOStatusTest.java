package com.hartwig.actin.algo.evaluation.general;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasMaximumWHOStatusTest {

    @Test
    public void canEvaluate() {
        HasMaximumWHOStatus function = new HasMaximumWHOStatus(2);

        assertEquals(Evaluation.PASS, function.evaluate(patientWithWHO(0)));
        assertEquals(Evaluation.FAIL, function.evaluate(patientWithWHO(4)));
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(patientWithWHO(null)));
    }

    @NotNull
    private static PatientRecord patientWithWHO(@Nullable Integer who) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .clinicalStatus(ImmutableClinicalStatus.builder().who(who).build())
                        .build())
                .build();
    }
}