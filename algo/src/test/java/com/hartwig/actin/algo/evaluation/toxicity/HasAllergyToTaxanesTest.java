package com.hartwig.actin.algo.evaluation.toxicity;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Allergy;

import org.junit.Test;

public class HasAllergyToTaxanesTest {

    @Test
    public void canEvaluate() {
        HasAllergyToTaxanes function = new HasAllergyToTaxanes();

        // No allergies
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withAllergies(Lists.newArrayList())));

        // Mismatch allergy
        Allergy mismatch = ToxicityTestFactory.allergy().name("mismatch").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withAllergy(mismatch)));

        // Matching allergy
        Allergy match = ToxicityTestFactory.allergy().name(HasAllergyToTaxanes.TAXANES.iterator().next()).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withAllergy(match)));
    }
}