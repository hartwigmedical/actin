package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasBoneMetastasesTest {

    @Test
    public void canEvaluate() {
        HasBoneMetastases function = new HasBoneMetastases();

        assertEquals(EvaluationResult.PASS, function.evaluate(patientWithBoneLesions(true)).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(patientWithBoneLesions(false)).result());
        assertEquals(EvaluationResult.UNDETERMINED, function.evaluate(patientWithBoneLesions(null)).result());
    }

    @NotNull
    private static PatientRecord patientWithBoneLesions(@Nullable Boolean hasBoneLesions) {
        return TumorEvaluationTestUtil.withTumorDetails(ImmutableTumorDetails.builder().hasBoneLesions(hasBoneLesions).build());
    }
}