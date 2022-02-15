package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasKnownActiveBrainMetastasesTest {

    @Test
    public void canEvaluate() {
        HasKnownActiveBrainMetastases function = new HasKnownActiveBrainMetastases();

        assertEquals(EvaluationResult.PASS, function.evaluate(patientWithActiveBrainLesions(true)).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(patientWithActiveBrainLesions(false)).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(patientWithActiveBrainLesions(null)).result());
    }

    @NotNull
    private static PatientRecord patientWithActiveBrainLesions(@Nullable Boolean hasActiveBrainLesions) {
        return TumorEvaluationTestUtil.withTumorDetails(ImmutableTumorDetails.builder()
                .hasActiveBrainLesions(hasActiveBrainLesions)
                .build());
    }
}