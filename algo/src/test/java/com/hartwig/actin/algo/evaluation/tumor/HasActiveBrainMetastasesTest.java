package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasActiveBrainMetastasesTest {

    @Test
    public void canEvaluate() {
        HasActiveBrainMetastases function = new HasActiveBrainMetastases();

        assertEquals(Evaluation.PASS, function.evaluate(patientWithActiveBrainLesions(true)));
        assertEquals(Evaluation.FAIL, function.evaluate(patientWithActiveBrainLesions(false)));
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(patientWithActiveBrainLesions(null)));
    }

    @NotNull
    private static PatientRecord patientWithActiveBrainLesions(@Nullable Boolean hasActiveBrainLesions) {
        return TumorEvaluationTestUtil.withTumorDetails(ImmutableTumorDetails.builder()
                .hasActiveBrainLesions(hasActiveBrainLesions)
                .build());
    }
}