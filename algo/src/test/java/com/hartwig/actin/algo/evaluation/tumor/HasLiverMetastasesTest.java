package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasLiverMetastasesTest {

    @Test
    public void canEvaluate() {
        HasLiverMetastases function = new HasLiverMetastases();

        assertEquals(EvaluationResult.PASS, function.evaluate(patientWithLiverLesions(true)).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(patientWithLiverLesions(false)).result());
        assertEquals(EvaluationResult.UNDETERMINED, function.evaluate(patientWithLiverLesions(null)).result());
    }

    @NotNull
    private static PatientRecord patientWithLiverLesions(@Nullable Boolean hasLiverLesions) {
        return TumorEvaluationTestUtil.withTumorDetails(ImmutableTumorDetails.builder().hasLiverLesions(hasLiverLesions).build());
    }
}