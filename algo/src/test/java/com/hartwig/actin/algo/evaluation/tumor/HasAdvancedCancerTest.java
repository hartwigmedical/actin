package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasAdvancedCancerTest {

    @Test
    public void canEvaluate() {
        HasAdvancedCancer function = new HasAdvancedCancer();

        assertEquals(EvaluationResult.PASS, function.evaluate(patientWithTumorStage(TumorStage.IIIB)));
        assertEquals(EvaluationResult.FAIL, function.evaluate(patientWithTumorStage(TumorStage.II)));
        assertEquals(EvaluationResult.UNDETERMINED, function.evaluate(patientWithTumorStage(null)));
    }

    @NotNull
    private static PatientRecord patientWithTumorStage(@Nullable TumorStage stage) {
        return TumorEvaluationTestUtil.withTumorDetails(ImmutableTumorDetails.builder().stage(stage).build());
    }
}