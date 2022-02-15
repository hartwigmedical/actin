package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasKnownSymptomaticBrainMetastasesTest {

    @Test
    public void canEvaluate() {
        HasKnownSymptomaticBrainMetastases function = new HasKnownSymptomaticBrainMetastases();

        assertEquals(EvaluationResult.PASS, function.evaluate(patientWithSymptomaticBrainLesions(true)));
        assertEquals(EvaluationResult.FAIL, function.evaluate(patientWithSymptomaticBrainLesions(false)));
        assertEquals(EvaluationResult.FAIL, function.evaluate(patientWithSymptomaticBrainLesions(null)));
    }

    @NotNull
    private static PatientRecord patientWithSymptomaticBrainLesions(@Nullable Boolean hasSymptomaticBrainLesions) {
        return TumorEvaluationTestUtil.withTumorDetails(ImmutableTumorDetails.builder()
                .hasSymptomaticBrainLesions(hasSymptomaticBrainLesions)
                .build());
    }
}