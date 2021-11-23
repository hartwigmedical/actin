package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasBrainMetastasesTest {

    @Test
    public void canEvaluate() {
        HasBrainMetastases function = new HasBrainMetastases();

        assertEquals(Evaluation.PASS, function.evaluate(patientWithBrainLesions(true)));
        assertEquals(Evaluation.FAIL, function.evaluate(patientWithBrainLesions(false)));
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(patientWithBrainLesions(null)));
    }

    @NotNull
    private static PatientRecord patientWithBrainLesions(@Nullable Boolean hasBrainLesions) {
        return TumorTestUtil.withTumorDetails(ImmutableTumorDetails.builder().hasBrainLesions(hasBrainLesions).build());
    }

}