package com.hartwig.actin.algo.evaluation.cardiacfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasSufficientLVEFTest {

    @Test
    public void canEvaluate() {
        HasSufficientLVEF function = new HasSufficientLVEF(0.71, false);

        // No LVEF known
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withLVEF(null)));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withLVEF(0.1)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withLVEF(0.71)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withLVEF(0.9)));

        HasSufficientLVEF functionWithPass = new HasSufficientLVEF(0.71, true);
        assertEvaluation(EvaluationResult.PASS, functionWithPass.evaluate(withLVEF(null)));
    }

    @NotNull
    private static PatientRecord withLVEF(@Nullable Double lvef) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .clinicalStatus(ImmutableClinicalStatus.builder().lvef(lvef).build())
                        .build())
                .build();
    }
}