package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasStomachUndifferentiatedTumorTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();
        HasStomachUndifferentiatedTumor function = new HasStomachUndifferentiatedTumor(doidModel);

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids((Set<String>) null)));

        PatientRecord missingType = TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                .addDoids(DoidConstants.STOMACH_CANCER_DOID)
                .primaryTumorType("wrong")
                .build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(missingType));

        PatientRecord missingDoid = TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                .addDoids("wrong")
                .primaryTumorType(HasStomachUndifferentiatedTumor.UNDIFFERENTIATED_TYPES.iterator().next())
                .build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(missingDoid));

        PatientRecord correct = TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                .addDoids(DoidConstants.STOMACH_CANCER_DOID)
                .primaryTumorType(HasStomachUndifferentiatedTumor.UNDIFFERENTIATED_TYPES.iterator().next())
                .build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(correct));
    }
}