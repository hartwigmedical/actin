package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasTumorStageTest {

    @Test
    public void canEvaluate() {
        HasTumorStage function = new HasTumorStage(TestDoidModelFactory.createMinimalTestDoidModel(), TumorStage.III);

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(null)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.III)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIIB)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IV)));
    }

    @Test
    public void canEvaluateBasedOnLesions() {
        HasTumorStage function = new HasTumorStage(TestDoidModelFactory.createMinimalTestDoidModel(), TumorStage.IV);

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder().hasLiverLesions(true).build())));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                        .hasLiverLesions(true)
                        .addDoids(HasTumorStage.LIVER_CANCER_DOID)
                        .build())));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder().hasCnsLesions(true).build())));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                        .hasCnsLesions(true)
                        .addDoids(HasTumorStage.CNS_CANCER_DOID)
                        .build())));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder().hasBrainLesions(true).build())));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                        .hasBrainLesions(true)
                        .addDoids(HasTumorStage.BRAIN_CANCER_DOID)
                        .build())));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder().hasLungLesions(true).build())));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                        .hasLungLesions(true)
                        .addDoids(HasTumorStage.LUNG_CANCER_DOID)
                        .build())));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder().hasBoneLesions(true).build())));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withTumorDetails(TumorTestFactory.builder()
                        .hasBoneLesions(true)
                        .addDoids(HasTumorStage.BONE_CANCER_DOID)
                        .build())));

    }
}