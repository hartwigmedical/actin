package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Set;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.actin.treatment.input.datamodel.TumorTypeInput;

import org.junit.Test;

public class HasCancerOfUnknownPrimaryTest {

    @Test
    public void canEvaluate() {
        TumorTypeInput category = TumorTypeInput.ADENOCARCINOMA;
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(category.doid(), "200");

        HasCancerOfUnknownPrimary function = new HasCancerOfUnknownPrimary(doidModel, category);

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids((Set<String>) null)));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TumorTestFactory.withDoids(HasCancerOfUnknownPrimary.CANCER_DOID)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("random doid")));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids(category.doid(), "other doid")));

        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids(category.doid())));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids(category.doid(), "200")));
    }
}