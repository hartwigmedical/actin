package com.hartwig.actin.algo.evaluation.toxicity;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasIntoleranceWithSpecificDoidTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child");
        HasIntoleranceWithSpecificDoid function = new HasIntoleranceWithSpecificDoid(doidModel, "parent");

        // No allergies
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerances(Lists.newArrayList())));

        // Allergy with mismatching doid
        Intolerance mismatch = ToxicityTestFactory.intolerance().addDoids("other").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerance(mismatch)));

        // Matching with parent doid
        Intolerance matchParent = ToxicityTestFactory.intolerance().addDoids("other", "parent").build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withIntolerance(matchParent)));

        // Matching with child doid
        Intolerance matchChild = ToxicityTestFactory.intolerance().addDoids("child").build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withIntolerance(matchChild)));
    }
}