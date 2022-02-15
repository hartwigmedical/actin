package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasKnownSymptomaticCnsMetastasesTest {

    @Test
    public void canEvaluate() {
        HasKnownSymptomaticCnsMetastases function = new HasKnownSymptomaticCnsMetastases();

        assertEquals(EvaluationResult.PASS, function.evaluate(patientWithSymptomaticCnsLesions(true)).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(patientWithSymptomaticCnsLesions(false)).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(patientWithSymptomaticCnsLesions(null)).result());
    }

    @NotNull
    private static PatientRecord patientWithSymptomaticCnsLesions(@Nullable Boolean hasSymptomaticCnsLesions) {
        return TumorEvaluationTestUtil.withTumorDetails(ImmutableTumorDetails.builder()
                .hasSymptomaticCnsLesions(hasSymptomaticCnsLesions)
                .build());
    }
}