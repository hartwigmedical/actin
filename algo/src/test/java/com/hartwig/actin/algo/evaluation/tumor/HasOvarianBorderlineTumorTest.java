package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasOvarianBorderlineTumorTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();
        HasOvarianBorderlineTumor function = new HasOvarianBorderlineTumor(doidModel);

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids((Set<String>) null)));

        PatientRecord missingType = TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                .addDoids(HasOvarianBorderlineTumor.OVARIAN_CANCER_DOID)
                .primaryTumorType("wrong")
                .build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(missingType));

        PatientRecord missingDoid = TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                .addDoids("wrong")
                .primaryTumorType(HasOvarianBorderlineTumor.OVARIAN_BORDERLINE_TYPES.iterator().next())
                .build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(missingDoid));

        PatientRecord correct = TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                .addDoids(HasOvarianBorderlineTumor.OVARIAN_CANCER_DOID)
                .primaryTumorType(HasOvarianBorderlineTumor.OVARIAN_BORDERLINE_TYPES.iterator().next())
                .build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(correct));
    }
}