package com.hartwig.actin.algo.evaluation.toxicity;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Intolerance;

import org.junit.Test;

public class HasAllergyToTaxanesTest {

    @Test
    public void canEvaluate() {
        HasAllergyToTaxanes function = new HasAllergyToTaxanes();

        // No allergies
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerances(Lists.newArrayList())));

        // Mismatch allergy
        Intolerance mismatch = ToxicityTestFactory.intolerance().name("mismatch").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerance(mismatch)));

        // Matching allergy
        Intolerance match = ToxicityTestFactory.intolerance().name(HasAllergyToTaxanes.TAXANES.iterator().next()).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withIntolerance(match)));
    }
}