package com.hartwig.actin.algo.evaluation.toxicity;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.Allergy;

import org.junit.Test;

public class HasAllergyWithSpecificDoidTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child");
        HasAllergyWithSpecificDoid function = new HasAllergyWithSpecificDoid(doidModel, "parent");

        // No allergies
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withAllergies(Lists.newArrayList())));

        // Allergy with mismatching doid
        Allergy mismatch = ToxicityTestFactory.allergy().addDoids("other").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withAllergy(mismatch)));

        // Matching with parent doid
        Allergy matchParent = ToxicityTestFactory.allergy().addDoids("other", "parent").build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withAllergy(matchParent)));

        // Matching with child doid
        Allergy matchChild = ToxicityTestFactory.allergy().addDoids("child").build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withAllergy(matchChild)));
    }
}