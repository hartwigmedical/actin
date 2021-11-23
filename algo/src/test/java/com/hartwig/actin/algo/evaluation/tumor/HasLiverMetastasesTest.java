package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasLiverMetastasesTest {

    @Test
    public void canEvaluate() {
        HasLiverMetastases function = new HasLiverMetastases();

        assertEquals(Evaluation.PASS, function.evaluate(patientWithLiverLesions(true)));
        assertEquals(Evaluation.FAIL, function.evaluate(patientWithLiverLesions(false)));
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(patientWithLiverLesions(null)));
    }

    @NotNull
    private static PatientRecord patientWithLiverLesions(@Nullable Boolean hasLiverLesions) {
        return TumorTestUtil.withTumorDetails(ImmutableTumorDetails.builder().hasLiverLesions(hasLiverLesions).build());
    }
}