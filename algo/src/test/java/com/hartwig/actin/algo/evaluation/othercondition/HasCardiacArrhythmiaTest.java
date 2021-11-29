package com.hartwig.actin.algo.evaluation.othercondition;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableECGAberration;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasCardiacArrhythmiaTest {

    @Test
    public void canEvaluate() {
        HasCardiacArrhythmia function = new HasCardiacArrhythmia();

        assertEquals(Evaluation.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        assertEquals(Evaluation.PASS, function.evaluate(withEcgAberration(true)));
        assertEquals(Evaluation.FAIL, function.evaluate(withEcgAberration(false)));
    }

    @NotNull
    private static PatientRecord withEcgAberration(boolean hasSignificantEcgAberration) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .clinicalStatus(ImmutableClinicalStatus.builder()
                                .ecgAberration(ImmutableECGAberration.builder()
                                        .hasSigAberrationLatestECG(hasSignificantEcgAberration)
                                        .description(Strings.EMPTY)
                                        .build())
                                .build())
                        .build())
                .build();
    }
}