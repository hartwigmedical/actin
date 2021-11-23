package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasAdvancedCancerTest {

    @Test
    public void canEvaluate() {
        HasAdvancedCancer function = new HasAdvancedCancer();

        assertEquals(Evaluation.PASS, function.evaluate(patientWithTumorStage(TumorStage.IIIB)));
        assertEquals(Evaluation.FAIL, function.evaluate(patientWithTumorStage(TumorStage.II)));
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(patientWithTumorStage(null)));
    }

    @NotNull
    private static PatientRecord patientWithTumorStage(@Nullable TumorStage stage) {
        return TumorTestUtil.withTumorDetails(ImmutableTumorDetails.builder().stage(stage).build());
    }
}