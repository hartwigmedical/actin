package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasBreastCancerHormonePositiveHER2NegativeTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();
        HasBreastCancerHormonePositiveHER2Negative function = new HasBreastCancerHormonePositiveHER2Negative(doidModel);

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids((Set<String>) null)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("wrong")));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withDoids(HasBreastCancerHormonePositiveHER2Negative.HER2_NEGATIVE_BREAST_CANCER_DOID,
                        HasBreastCancerHormonePositiveHER2Negative.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID)));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TumorTestFactory.withDoids(HasBreastCancerHormonePositiveHER2Negative.HER2_NEGATIVE_BREAST_CANCER_DOID,
                        HasBreastCancerHormonePositiveHER2Negative.ESTROGEN_POSITIVE_BREAST_CANCER_DOID)));

        assertEvaluation(EvaluationResult.WARN,
                function.evaluate(TumorTestFactory.withDoids(HasBreastCancerHormonePositiveHER2Negative.ESTROGEN_POSITIVE_BREAST_CANCER_DOID)));

        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withDoids(HasBreastCancerHormonePositiveHER2Negative.BREAST_CANCER_DOID)));

        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(TumorTestFactory.withDoids(HasBreastCancerHormonePositiveHER2Negative.BREAST_CANCER_DOID,
                        HasBreastCancerHormonePositiveHER2Negative.HER2_POSITIVE_BREAST_CANCER_DOID)));

        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(TumorTestFactory.withDoids(HasBreastCancerHormonePositiveHER2Negative.BREAST_CANCER_DOID,
                        HasBreastCancerHormonePositiveHER2Negative.ESTROGEN_NEGATIVE_BREAST_CANCER_DOID)));

        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(TumorTestFactory.withDoids(HasBreastCancerHormonePositiveHER2Negative.BREAST_CANCER_DOID,
                        HasBreastCancerHormonePositiveHER2Negative.PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID)));
    }

    @Test
    public void canCompareMolecularAndClinicalHer2Status() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();
        HasBreastCancerHormonePositiveHER2Negative function = new HasBreastCancerHormonePositiveHER2Negative(doidModel);

        Set<String> match = Sets.newHashSet(HasBreastCancerHormonePositiveHER2Negative.HER2_NEGATIVE_BREAST_CANCER_DOID,
                HasBreastCancerHormonePositiveHER2Negative.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID);

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoidsAndAmplification(match, "KRAS")));
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withDoidsAndAmplification(match, "ERBB2")));
    }
}