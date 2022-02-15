package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasMeasurableDiseaseRecistTest {

    @Test
    public void canEvaluate() {
        HasMeasurableDiseaseRecist function = new HasMeasurableDiseaseRecist();

        assertEquals(EvaluationResult.PASS, function.evaluate(patientWithMeasurableLesionRecist(true)));
        assertEquals(EvaluationResult.FAIL, function.evaluate(patientWithMeasurableLesionRecist(false)));
        assertEquals(EvaluationResult.UNDETERMINED, function.evaluate(patientWithMeasurableLesionRecist(null)));
    }

    @NotNull
    private static PatientRecord patientWithMeasurableLesionRecist(@Nullable Boolean hasMeasurableLesionRecist) {
        return TumorEvaluationTestUtil.withTumorDetails(ImmutableTumorDetails.builder()
                .hasMeasurableLesionRecist(hasMeasurableLesionRecist)
                .build());
    }
}