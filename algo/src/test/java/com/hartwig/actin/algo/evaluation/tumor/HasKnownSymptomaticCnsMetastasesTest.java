package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasKnownSymptomaticCnsMetastasesTest {

    @Test
    public void canEvaluate() {
        HasSymptomaticCnsMetastases function = new HasSymptomaticCnsMetastases();

        assertEquals(Evaluation.PASS, function.evaluate(patientWithSymptomaticCnsLesions(true)));
        assertEquals(Evaluation.FAIL, function.evaluate(patientWithSymptomaticCnsLesions(false)));
        assertEquals(Evaluation.FAIL, function.evaluate(patientWithSymptomaticCnsLesions(null)));
    }

    @NotNull
    private static PatientRecord patientWithSymptomaticCnsLesions(@Nullable Boolean hasSymptomaticCnsLesions) {
        return TumorEvaluationTestUtil.withTumorDetails(ImmutableTumorDetails.builder()
                .hasSymptomaticCnsLesions(hasSymptomaticCnsLesions)
                .build());
    }
}