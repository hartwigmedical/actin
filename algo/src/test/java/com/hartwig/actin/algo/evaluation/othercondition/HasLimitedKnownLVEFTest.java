package com.hartwig.actin.algo.evaluation.othercondition;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasLimitedKnownLVEFTest {

    @Test
    public void canEvaluate() {
        HasLimitedKnownLVEF function = new HasLimitedKnownLVEF(0.71);

        // No LVEF known
        assertEquals(Evaluation.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        assertEquals(Evaluation.PASS, function.evaluate(withLVEF(0.1)));
        assertEquals(Evaluation.PASS, function.evaluate(withLVEF(0.71)));
        assertEquals(Evaluation.FAIL, function.evaluate(withLVEF(0.9)));
    }

    @NotNull
    private static PatientRecord withLVEF(double lvef) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .clinicalStatus(ImmutableClinicalStatus.builder().lvef(lvef).build())
                        .build())
                .build();
    }
}