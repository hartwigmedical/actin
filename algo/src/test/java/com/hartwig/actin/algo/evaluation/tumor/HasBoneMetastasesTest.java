package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasBoneMetastasesTest {

    @Test
    public void canEvaluate() {
        HasBoneMetastases function = new HasBoneMetastases();

        assertEquals(Evaluation.PASS, function.evaluate(patientWithBoneLesions(true)));
        assertEquals(Evaluation.FAIL, function.evaluate(patientWithBoneLesions(false)));
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(patientWithBoneLesions(null)));
    }

    @NotNull
    private static PatientRecord patientWithBoneLesions(@Nullable Boolean hasBoneLesions) {
        return TumorEvaluationTestUtil.withTumorDetails(ImmutableTumorDetails.builder().hasBoneLesions(hasBoneLesions).build());
    }
}