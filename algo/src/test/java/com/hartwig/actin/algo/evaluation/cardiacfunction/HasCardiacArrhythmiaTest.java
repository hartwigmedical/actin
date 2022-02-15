package com.hartwig.actin.algo.evaluation.cardiacfunction;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableECG;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasCardiacArrhythmiaTest {

    @Test
    public void canEvaluate() {
        HasCardiacArrhythmia function = new HasCardiacArrhythmia();

        assertEquals(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()).result());

        assertEquals(EvaluationResult.PASS, function.evaluate(withHasSignificantECGAberration(true)).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(withHasSignificantECGAberration(false)).result());
    }

    @NotNull
    private static PatientRecord withHasSignificantECGAberration(boolean hasSignificantECGAberration) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .clinicalStatus(ImmutableClinicalStatus.builder()
                                .ecg(ImmutableECG.builder()
                                        .hasSigAberrationLatestECG(hasSignificantECGAberration)
                                        .aberrationDescription(Strings.EMPTY)
                                        .build())
                                .build())
                        .build())
                .build();
    }
}