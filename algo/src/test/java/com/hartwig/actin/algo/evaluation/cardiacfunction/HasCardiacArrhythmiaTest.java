package com.hartwig.actin.algo.evaluation.cardiacfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableECG;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasCardiacArrhythmiaTest {

    @Test
    public void canEvaluateWithoutType() {
        HasCardiacArrhythmia function = new HasCardiacArrhythmia(null);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withECG(null)));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withHasSignificantECGAberration(false)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withHasSignificantECGAberration(true)));
    }

    @Test
    public void canEvaluateWithType() {
        HasCardiacArrhythmia function = new HasCardiacArrhythmia("serious");

        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(withECG(ImmutableECG.builder()
                        .hasSigAberrationLatestECG(true)
                        .aberrationDescription("Fine condition")
                        .build())));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(withECG(ImmutableECG.builder()
                        .hasSigAberrationLatestECG(true)
                        .aberrationDescription("Serious condition")
                        .build())));
    }

    @NotNull
    private static PatientRecord withHasSignificantECGAberration(boolean hasSignificantECGAberration) {
        return withECG(ImmutableECG.builder()
                .hasSigAberrationLatestECG(hasSignificantECGAberration)
                .aberrationDescription(Strings.EMPTY)
                .build());
    }

    @NotNull
    private static PatientRecord withECG(@Nullable ECG ecg) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .clinicalStatus(ImmutableClinicalStatus.builder().ecg(ecg).build())
                        .build())
                .build();
    }
}