package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasSymptomaticBrainMetastasesTest {

    @Test
    public void canEvaluate() {
        HasSymptomaticBrainMetastases function = new HasSymptomaticBrainMetastases();

        assertEquals(Evaluation.PASS, function.evaluate(patientWithSymptomaticBrainLesions(true)));
        assertEquals(Evaluation.FAIL, function.evaluate(patientWithSymptomaticBrainLesions(false)));
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(patientWithSymptomaticBrainLesions(null)));
    }

    @NotNull
    private static PatientRecord patientWithSymptomaticBrainLesions(@Nullable Boolean hasSymptomaticBrainLesions) {
        return TumorEvaluationTestUtil.withTumorDetails(ImmutableTumorDetails.builder()
                .hasSymptomaticBrainLesions(hasSymptomaticBrainLesions)
                .build());
    }
}